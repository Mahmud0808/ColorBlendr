package com.drdisagree.colorblendr.xposed.modules;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.colorblendr.config.XPrefs.Xprefs;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

import android.content.Context;
import android.os.Build;

import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.xposed.ModPack;
import com.drdisagree.colorblendr.xposed.modules.utils.ColorModifiers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MonetColors extends ModPack implements IXposedHookLoadPackage {

    @SuppressWarnings("unused")
    private static final String TAG = "ColorBlendr: ";
    private int monetAccentSaturation = 100;
    private int monetBackgroundSaturation = 100;
    private int monetBackgroundLightness = 100;
    private boolean pitchBlackTheme = false;
    private AtomicInteger counter;
    private Class<?> ThemeOverlayControllerParentClass;
    private Method reevaluateSystemTheme;
    private XC_MethodHook.MethodHookParam ThemeOverlayControllerParam;
    private final List<Integer> SHADE_KEYS = List.of(10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000);
    private int seedColor = -1;
    private boolean firstHookTried, firstHookSuccess;
    private boolean secondHookTried, secondHookSuccess;

    public MonetColors(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        monetAccentSaturation = Xprefs.getInt(MONET_ACCENT_SATURATION, 100);
        monetBackgroundSaturation = Xprefs.getInt(MONET_BACKGROUND_SATURATION, 100);
        monetBackgroundLightness = Xprefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100);
        pitchBlackTheme = Xprefs.getBoolean(MONET_PITCH_BLACK_THEME, false);
        seedColor = Xprefs.getInt(MONET_SEED_COLOR, -1);

        if (Key.length > 0 && (Key[0].equals(MONET_ACCENT_SATURATION) ||
                Key[0].equals(MONET_BACKGROUND_SATURATION) ||
                Key[0].equals(MONET_BACKGROUND_LIGHTNESS) ||
                Key[0].equals(MONET_PITCH_BLACK_THEME) ||
                Key[0].equals(MONET_SEED_COLOR)
        )) {
            try {
                if (ThemeOverlayControllerParam != null) {
                    callMethod(ThemeOverlayControllerParam.thisObject, "reevaluateSystemTheme", true);
                }
            } catch (Throwable ignored) {
                try {
                    if (reevaluateSystemTheme != null) {
                        reevaluateSystemTheme.invoke(ThemeOverlayControllerParam.thisObject, true);
                    }
                } catch (Throwable throwable) {
                    log(TAG + throwable);
                }
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> ThemeOverlayControllerClass = findClass(SYSTEMUI_PACKAGE + ".theme.ThemeOverlayController", loadPackageParam.classLoader);

        hookAllConstructors(ThemeOverlayControllerClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                ThemeOverlayControllerParam = param;

                ThemeOverlayControllerParentClass = param.thisObject.getClass().getSuperclass();
                reevaluateSystemTheme = findMethodExact(ThemeOverlayControllerParentClass, "reevaluateSystemTheme", boolean.class);
            }
        });

        counter = new AtomicInteger(0);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2) {
            // Android 12 & 12.1
            Class<?> ShadesClass = findClass(SYSTEMUI_PACKAGE + ".monet.Shades", loadPackageParam.classLoader);

            hookAllMethods(ShadesClass, "of", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    float hue = (float) param.args[0];
                    float chroma = (float) param.args[1];

                    if (seedColor != -1) {
                        hue = ColorUtil.getHue(seedColor);
                    }

                    ArrayList<Integer> shadesList = ColorModifiers.generateShades(hue, chroma);
                    ArrayList<Integer> modifiedShades = ColorModifiers.modifyColors(
                            shadesList,
                            counter,
                            monetAccentSaturation,
                            monetBackgroundSaturation,
                            monetBackgroundLightness,
                            pitchBlackTheme
                    );

                    int[] shades = modifiedShades.stream()
                            .mapToInt(Integer::intValue)
                            .toArray();

                    param.setResult(shades);

                    log(TAG + "hue: " + hue + " chroma: " + chroma + " isAccent: " + (counter.get() <= 3 && counter.get() != 0));
                }
            });
        } else {
            firstHookTried = false;
            firstHookSuccess = false;
            secondHookTried = false;
            secondHookSuccess = false;

            // Android 13
            try {
                Class<?> TonalSpecClass = findClass(SYSTEMUI_PACKAGE + ".monet.TonalSpec", loadPackageParam.classLoader);

                firstHookTried = true;

                hookAllMethods(TonalSpecClass, "shades", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            Object hue = getObjectField(param.thisObject, "hue");
                            float hueValue = (float) ((double) callMethod(hue, "get", param.args[0]));

                            if (seedColor != -1) {
                                hueValue = ColorUtil.getHue(seedColor);
                            }

                            Object chroma = getObjectField(param.thisObject, "chroma");
                            float chromaValue = (float) ((double) callMethod(chroma, "get", param.args[0]));

                            ArrayList<Integer> shadesList = ColorModifiers.generateShades(hueValue, chromaValue);
                            ArrayList<Integer> modifiedShades = ColorModifiers.modifyColors(
                                    shadesList,
                                    counter,
                                    monetAccentSaturation,
                                    monetBackgroundSaturation,
                                    monetBackgroundLightness,
                                    pitchBlackTheme
                            );

                            param.setResult(modifiedShades);

                            firstHookSuccess = true;

                            log(TAG + "hue: " + hueValue + " chroma: " + chromaValue + " isAccent: " + (counter.get() <= 3 && counter.get() != 0));
                        } catch (Throwable throwable) {
                            if (!firstHookSuccess && !secondHookSuccess && secondHookTried) {
                                log(TAG + throwable);
                            }
                        }
                    }
                });
            } catch (Throwable throwable) {
                if (firstHookTried && secondHookTried) {
                    log(TAG + throwable);
                }
            }

            // Android 14
            try {
                Class<?> TonalPaletteClass = findClass(SYSTEMUI_PACKAGE + ".monet.TonalPalette", loadPackageParam.classLoader);

                secondHookTried = true;

                hookAllConstructors(TonalPaletteClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Object camColor = getObjectField(param.thisObject, "seedCam");

                            Object hue = getObjectField(param.args[0], "hue");
                            float hueValue = (float) ((double) callMethod(hue, "get", camColor));

                            if (seedColor != -1) {
                                hueValue = ColorUtil.getHue(seedColor);
                            }

                            Object chroma = getObjectField(param.args[0], "chroma");
                            float chromaValue = (float) ((double) callMethod(chroma, "get", camColor));

                            ArrayList<Integer> shadesList = ColorModifiers.generateShades(hueValue, chromaValue);
                            ArrayList<Integer> modifiedShades = ColorModifiers.modifyColors(
                                    shadesList,
                                    counter,
                                    monetAccentSaturation,
                                    monetBackgroundSaturation,
                                    monetBackgroundLightness,
                                    pitchBlackTheme
                            );

                            Map<Integer, Integer> mappedShades = ColorModifiers.zipToMap(SHADE_KEYS, modifiedShades);

                            setObjectField(param.thisObject, "allShades", modifiedShades);
                            setObjectField(param.thisObject, "allShadesMapped", mappedShades);

                            secondHookSuccess = true;

                            log(TAG + "hue: " + hueValue + " chroma: " + chromaValue + " isAccent: " + (counter.get() <= 3 && counter.get() != 0));
                        } catch (Throwable throwable) {
                            if (!firstHookSuccess && !secondHookSuccess && firstHookTried) {
                                log(TAG + throwable);
                            }
                        }
                    }
                });
            } catch (Throwable throwable) {
                if (firstHookTried && secondHookTried) {
                    log(TAG + throwable);
                }
            }
        }
    }
}