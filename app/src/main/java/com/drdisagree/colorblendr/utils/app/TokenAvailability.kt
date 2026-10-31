package com.drdisagree.colorblendr.utils.app

import com.drdisagree.colorblendr.data.common.Constant.FRAMEWORK_PACKAGE
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenGroup

object TokenAvailability {

    val tokens: Set<Token> by lazy {
        Token.entries.filter { token ->
            token.group != TokenGroup.CUSTOM ||
                    ResourceUtil.resourceExists(FRAMEWORK_PACKAGE, "system_${token.resourceName}_dark", "color")
        }.toSet()
    }
}
