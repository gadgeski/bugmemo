// app/src/test/java/com/example/bugmemo/ExampleUnitTest.kt

package com.example.bugmemo

import org.junit.Assert.assertEquals // ★ Changed: ワイルドカードを個別 import に変更
import org.junit.Test               // （そのまま利用）

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}