
import org.junit.Test
import kotlin.test.assertEquals
import com.squareup.moshi.Moshi
import org.godotengine.plugin.android.notifications.types.data.BasicNotification
import org.godotengine.plugin.android.notifications.types.data.NotificationData
import org.godotengine.plugin.android.notifications.types.data.NotificationWithImage
import kotlin.test.assertTrue

data class TestObj(var data: String)

class NotificationDataTest {
    private val moshi: Moshi = NotificationData.getMoshiBuilder().build()
    private val adapterTest = moshi.adapter(TestObj::class.java)
    private val adapterNotificationData = moshi.adapter(NotificationData::class.java)


    @Test
    fun testSerialize() {
        val test = TestObj("info")

        val json = adapterTest.toJson(test)
        println(json) // {"data":"info"}

        val obj = adapterTest.fromJson(json)
        assertEquals(test, obj)
    }

    @Test
    fun testSerializeBaseNotification() {
        val notification = BasicNotification("Title", "Content", "icon.png")
        val json = adapterNotificationData.toJson(notification)
        val deserialized = adapterNotificationData.fromJson(json)

        assertEquals(notification, deserialized)
        assertTrue { deserialized is NotificationData }
        assertTrue { deserialized !is NotificationWithImage }
        println(deserialized)
    }

    @Test
    fun testNotificationWithImageSerialization() {
        val notification: NotificationData = NotificationWithImage(
            title = "title",
            content = "casa",
            image = "image"
        )

        val json = adapterNotificationData.toJson(notification)
        val obj = adapterNotificationData.fromJson(json)

        assertEquals(notification, obj)
        assertTrue(obj is NotificationWithImage)
        println(obj)
    }
}
