package com.example.dao

interface DAOUsersAuthDigestsFacade {
    suspend fun createDigest(userId: Int, digest: ByteArray)
    suspend fun validateDigest(digest: String): Boolean
}