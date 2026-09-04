package com.leon.repository

import com.leon.model.IoiBlock
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IoiBlockRepository : MongoRepository<IoiBlock, String>
