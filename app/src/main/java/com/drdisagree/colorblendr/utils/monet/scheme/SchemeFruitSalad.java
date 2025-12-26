/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.drdisagree.colorblendr.utils.monet.scheme;

import com.drdisagree.colorblendr.utils.monet.dynamiccolor.ColorSpec;
import com.drdisagree.colorblendr.utils.monet.dynamiccolor.ColorSpecs;
import com.drdisagree.colorblendr.utils.monet.dynamiccolor.DynamicScheme;
import com.drdisagree.colorblendr.utils.monet.dynamiccolor.Variant;
import com.drdisagree.colorblendr.utils.monet.hct.Hct;
import com.drdisagree.colorblendr.utils.monet.palettes.TonalPalette;
import com.drdisagree.colorblendr.utils.monet.utils.MathUtils;

/** A playful theme - the source color's hue does not appear in the theme. */
public class SchemeFruitSalad extends DynamicScheme {

    public SchemeFruitSalad(Hct sourceColorHct, boolean isDark, double contrastLevel) {
        this(sourceColorHct, isDark, contrastLevel, DEFAULT_SPEC_VERSION, DEFAULT_PLATFORM);
    }

    public SchemeFruitSalad(
            Hct sourceColorHct,
            boolean isDark,
            double contrastLevel,
            ColorSpec.SpecVersion specVersion,
            Platform platform) {
        super(
                sourceColorHct,
                Variant.FRUIT_SALAD,
                isDark,
                contrastLevel,
                platform,
                specVersion,
                ColorSpecs.get(specVersion)
                        .getPrimaryPalette(
                                Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel),
                ColorSpecs.get(specVersion)
                        .getSecondaryPalette(
                                Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel),
                ColorSpecs.get(specVersion)
                        .getTertiaryPalette(
                                Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel),
                ColorSpecs.get(specVersion)
                        .getNeutralPalette(
                                Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel),
                ColorSpecs.get(specVersion)
                        .getNeutralVariantPalette(
                                Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel),
                ColorSpecs.get(specVersion)
                        .getErrorPalette(Variant.FRUIT_SALAD, sourceColorHct, isDark, platform, contrastLevel));
    }
}
