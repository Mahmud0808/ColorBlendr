package com.drdisagree.colorblendr.data.repository

import com.drdisagree.colorblendr.data.dao.CustomStyleDao
import com.drdisagree.colorblendr.data.models.CustomStyleModel

class CustomStyleRepository(private val customStyleDao: CustomStyleDao) {
    suspend fun getCustomStyles(): List<CustomStyleModel> {
        return customStyleDao.getAllCustomStyles()
    }

    suspend fun saveCustomStyle(customStyle: CustomStyleModel) {
        customStyleDao.insertCustomStyle(customStyle)
    }

    suspend fun updateCustomStyle(customStyle: CustomStyleModel) {
        customStyleDao.updateCustomStyle(customStyle)
    }

    suspend fun deleteCustomStyle(customStyle: CustomStyleModel) {
        customStyleDao.deleteCustomStyle(customStyle)
    }

    suspend fun getCustomStyleById(styleId: String): CustomStyleModel? {
        return customStyleDao.getCustomStyleById(styleId)
    }
}