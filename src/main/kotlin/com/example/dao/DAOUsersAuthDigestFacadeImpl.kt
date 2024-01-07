package com.example.dao

import com.example.models.UserAuthDigest
import com.example.models.UsersDigests
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select

class DAOUsersAuthDigestFacadeImpl: DAOUsersAuthDigestsFacade {
    override suspend fun createDigest(userId: Int, digest: ByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun validateDigest(digest: String): Boolean {
        return DatabaseSingleton.dbQuery {
            val result = UsersDigests
                .select { UsersDigests.digest eq digest }
                .map(::resultRowToUserAuthDigest)
                .singleOrNull()

            result != null
        }
    }

    private fun resultRowToUserAuthDigest(row: ResultRow): UserAuthDigest {
        return UserAuthDigest(
            userId = row[UsersDigests.id],
            digest = row[UsersDigests.digest]
        )
    }
}