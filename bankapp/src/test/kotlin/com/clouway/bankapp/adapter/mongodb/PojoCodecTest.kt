package com.clouway.bankapp.adapter.mongodb

import com.github.fakemongo.junit.FongoRule
import com.google.appengine.repackaged.com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.`is` as Is
import org.bson.codecs.pojo.annotations.*

enum class TestEnum{
    ONE, TWO
}

data class TestData(val data: String,
                    val enum: TestEnum,
                    val date: LocalDateTime)

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PojoCodecTest {

    @Rule
    @JvmField
    val fongoRule = FongoRule()

    lateinit var db: MongoDatabase
    lateinit var coll: MongoCollection<Document>

    @Before
    fun setUp() {
        val pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build()

        db = fongoRule.mongoClient.getDatabase("test")

        //Get the default codec
        var defaulCodecRegistry : CodecRegistry = db.codecRegistry

        //Combine the default codec with the pojo codec
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
                defaulCodecRegistry,
                CodecRegistries.fromProviders(pojoCodecProvider))
        coll = db.getCollection("test").withCodecRegistry(
                pojoCodecRegistry)
    }

    @Test
    fun insertPojoByJSON(){
        val time = LocalDateTime.of(1, 1, 1, 1, 1, 1, 1)
        val pojo = TestData("123", TestEnum.ONE, time)
        coll.insertOne(Document.parse(Gson().toJson(pojo)))
    }

    @Test
    fun convertPojoToDocument(){
        val time = LocalDateTime.of(1, 1, 1, 1, 1, 1, 1)
        val document = Document()
        val testData = TestData("123", TestEnum.ONE, time)
        document.append("_id", 1L)
        document.append("obj", testData)
        coll.insertOne(document)

        Assert.assertThat(
                coll.find(Filters.eq("_id", 1L)).first()["obj"] as Document,
                Is(Document(mapOf(
                        "data" to "123",
                        "date" to Document(
                                mapOf(
                                        "dayOfMonth" to 1,
                                        "dayOfWeek" to "MONDAY",
                                        "dayOfYear" to 1,
                                        "hour" to 1,
                                        "minute" to 1,
                                        "month" to "JANUARY",
                                        "monthValue" to 1,
                                        "nano" to 1,
                                        "second" to 1,
                                        "year" to 1
                                )
                        ),
                        "enum" to "ONE"
                )))
        )
    }

}