package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.bankapp.core.UserRepository
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Indexes
import org.bson.Document
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoUserRepository(private val dbName: String,
                          private val client: MongoClient) : UserRepository {

    private val USER_COLLECTION = "Users"

    /**
     * "A MongoDB client with internal connection pooling.
     * For most applications, you should have one MongoClient instance for the entire JVM."
     * ref: http://mongodb.github.io/mongo-java-driver/3.6/javadoc/com/mongodb/MongoClient.html
     */
    private val collection: MongoCollection<Document>
        get() = client.getDatabase(dbName).getCollection(USER_COLLECTION)

    /**
     * Create an index on the username field of this collection.
     */
    init {
        collection.createIndex(Indexes.ascending("username"))
    }

    override fun getById(id: String): Optional<User> {
        val document = collection.find(eq("_id", id)).first() ?: return Optional.empty()
        return Optional.of(mapDocumentToUser(document))
    }

    override fun deleteById(id: String) {
        collection.deleteOne(eq("_id", id))
    }

    override fun update(user: User) {
        collection.replaceOne(eq("_id", user.id), mapUserToDocument(user))
    }

    override fun getByUsername(username: String): Optional<User> {
        val document = collection.find(eq("username", username)).first() ?: return Optional.empty()
        return Optional.of(mapDocumentToUser(document))
    }

    override fun registerIfNotExists(registerRequest: UserRegistrationRequest): User {
        val user = User(
                UUID.randomUUID().toString(),
                registerRequest.username,
                registerRequest.email,
                registerRequest.password
        )

        if(collection.find(eq("username", user.username)).first() == null){
            collection.insertOne(mapUserToDocument(user))
        }else throw UserAlreadyExistsException()

        return user
    }

    override fun checkPassword(user: User): Boolean {
        //Depracated in newer versions of interface
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun mapDocumentToUser(document: Document): User{
        return User(
                document.getString("_id"),
                document.getString("username"),
                document.getString("email"),
                document.getString("password")
        )
    }

    private fun mapUserToDocument(user: User): Document{
        val document = Document()

        document.append("_id", user.id)
        document.append("username", user.username)
        document.append("email", user.email)
        document.append("password", user.password)

        return document
    }
}