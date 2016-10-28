package com.anbillon.flabellum

import com.beust.klaxon.JsonObject
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

/**
 *power by
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MM.:  .:'   `:::  .:`MMMMMMMMMMM|`MMM'|MMMMMMMMMMM':  .:'   `:::  .:'.MM
MMMM.     :          `MMMMMMMMMM  :*'  MMMMMMMMMM'        :        .MMMM
MMMMM.    ::    .     `MMMMMMMM'  ::   `MMMMMMMM'   .     ::   .  .MMMMM
MMMMMM. :   :: ::'  :   :: ::'  :   :: ::'      :: ::'  :   :: ::.MMMMMM
MMMMMMM    ;::         ;::         ;::         ;::         ;::   MMMMMMM
MMMMMMM .:'   `:::  .:'   `:::  .:'   `:::  .:'   `:::  .:'   `::MMMMMMM
MMMMMM'     :           :           :           :           :    `MMMMMM
MMMMM'______::____      ::    .     ::    .     ::     ___._::____`MMMMM
MMMMMMMMMMMMMMMMMMM`---._ :: ::'  :   :: ::'  _.--::MMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMM::.         ::  .--MMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM-.     ;::-MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM. .:' .M:F_P:MMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM.   .MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM\ /MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMVMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
 * Created by Paper on 16/10/18.
 */

class TaskTest {

    @Test
    fun test() {
        val ret = camalName("name")
        println(ret)
        Assert.assertEquals(ret.first(),'N')
    }
    @Test
    fun testUpdateBean(){
        var json:JsonObject = JsonObject()
//        var file:File = File("../app/src/main/java/com/anbillon/flabellum/model/Book.java")
        var file:File = File("src/test/kotlin/com/anbillon/flabellum/Book.java")
        var file2:File = File("./")
        updateBean(file,json)
        Assert.assertTrue(true)
    }



    private fun updateBean(file:File,json: JsonObject){
        println("updateBean:")

        if (file.exists()){
//            var fr:FileInputStream = FileInputStream(file)
//            var ofi:ObjectInputStream = ObjectInputStream(fr)
            println("exit:")
            val url = file.toURI().toURL()
            val loader = URLClassLoader(arrayOf(url))//创建类加载器
            //import com.sun.org.apache.bcel.internal.util.ClassLoader;
            println(file.parent)
            var tool:JavaCompiler = ToolProvider.getSystemJavaCompiler()
            val result = tool.run(null,null,null,"-d","build/classes/test/",file.path)
            if (result == 0){
                println("success!")
            }else println("failed!")


            val classLoader = URLClassLoader(arrayOf<URL>(file.parentFile.parentFile.parentFile.parentFile.toURI().toURL()))
            println(file.parentFile.parentFile.parentFile.parentFile.toURI().toURL())
            // Load
//            val testClass = classLoader.loadClass(file.name.split(".")[0])
            println("xxx")
            val testClass = Class.forName("com.anbillon.flabellum."+file.name.split(".")[0])
            testClass.fields.forEach {
                println(it.name)
            }



//            URLClassLoader.getSystemClassLoader().loadClass().fields.forEach {
//                println(it.name)
//            }
            json.forEach { m ->

                if (m.value is Number) {
                } else if (m.value is Boolean) {
                } else if (m.value is String) {
                }else{
                }
            }
        }else{
            println("file not exit")
        }
    }


    private fun camalName(name: String): String {
        val first = name.first()
        val last = name.removeRange(0,1)
        return first.toUpperCase() + last
    }
}
