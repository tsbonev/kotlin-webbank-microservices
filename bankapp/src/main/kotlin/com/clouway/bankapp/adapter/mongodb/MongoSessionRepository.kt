package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.*
import com.clouway.entityhelper.toUtilDate
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.set
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoSessionRepository(private val dbName: String,
                             private val client: MongoClient,
                             private val getInstant: () -> LocalDateTime = { LocalDateTime.now() },
                             private val sessionRefreshDays: Long = 10
) : SessionRepository, SessionClearer, SessionCounter {

    private val SESSION_COLLECTION = "Sessions"

    private var coderRegistries: CodecRegistry

    /**
     * "A MongoDB client with internal connection pooling.
     * For most applications, you should have one MongoClient instance for the entire JVM."
     * ref: http://mongodb.github.io/mongo-java-driver/3.6/javadoc/com/mongodb/MongoClient.html
     */
    private val collection: MongoCollection<Document>
        get() = client.getDatabase(dbName).getCollection(SESSION_COLLECTION)
                .withCodecRegistry(coderRegistries) // pass the combined default and pojo codec to the collection
    // this allows POJOS to be saved against keys in the document map
    // POJOS are broken down to their serializable parts much like Gson() does

    /**
     * Creates a TTL index on expiresOn that will
     * delete the entry when the date is reached.
     */
    init {
        val db = client.getDatabase(dbName)

        //Get an automatic pojo codec provider
        val pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build()

        //Get the default codec
        val defaulCodecRegistry : CodecRegistry = db.codecRegistry

        //Combine the default codec with the pojo codec
        coderRegistries = CodecRegistries.fromRegistries(
                defaulCodecRegistry,
                CodecRegistries.fromProviders(pojoCodecProvider))

        collection.createIndex(Indexes.ascending("expiresOn"),
                IndexOptions().expireAfter(0, TimeUnit.MILLISECONDS))
    }

    override fun issueSession(sessionRequest: SessionRequest): Session {
        val session = Session(
                sessionRequest.userId,
                sessionRequest.sessionId,
                sessionRequest.expiration,
                sessionRequest.username,
                sessionRequest.userEmail,
                true
        )
        collection.insertOne(mapSessionToDocument(session))
        return session
    }

    override fun terminateSession(sessionId: String) {
        collection.deleteOne(eq("_id", sessionId))
    }

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {
        val retrievedSession = collection.find(
                and(
                        eq("_id", sessionId),
                        gt("expiresOn", date.toUtilDate())
                )
        ).first() ?: return Optional.empty()

        val session = mapDocumentToSession(retrievedSession)

        if (session.expiresOn.isBefore(getInstant().plusDays(sessionRefreshDays))) {
            refreshSession(sessionId)
        }

        return Optional.of(session)
    }

    override fun getActiveSessionsCount(): Long {
        return collection.count(
                gt("expiresOn", getInstant().toUtilDate()))
    }

    override fun deleteSessionsExpiringBefore(date: LocalDateTime) {
        collection.deleteMany(
                lte("expiresOn", date.toUtilDate())
        )
    }

    private fun refreshSession(sessionId: String) {
        collection.findOneAndUpdate(eq("_id", sessionId),
                set("expiresOn", getInstant().plusDays(sessionRefreshDays).toUtilDate()))
    }

    private fun mapSessionToDocument(session: Session): Document {
        val document = Document()
        document.append("_id", session.sessionId)
        document.append("userId", session.userId)
        document.append("userEmail", session.userEmail)
        document.append("username", session.username)
        document.append("expiresOn", session.expiresOn.toUtilDate())
        document.append("isAuthenticated", session.isAuthenticated)
        return document
    }

    private fun mapDocumentToSession(document: Document): Session {
        return Session(
                document.getString("userId"),
                document.getString("_id"),
                LocalDateTime.ofInstant(document.getDate("expiresOn").toInstant(), ZoneOffset.UTC),
                document.getString("username"),
                document.getString("userEmail"),
                document.getBoolean("isAuthenticated")
        )
    }
}