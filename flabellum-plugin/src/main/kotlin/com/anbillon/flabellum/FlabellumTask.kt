package com.anbillon.flabellum

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import uy.klutter.core.common.whenNotNull
import uy.klutter.core.uri.buildUri
import java.io.File
import java.io.FileReader
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import javax.lang.model.element.Modifier
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
 * Created by Paper on 16/10/15.
 */
open class FlabellumTask :SourceTask(){
    /**
     * default gen dir
     */
    val DEFAULT_GEN_DIR = "src/main/java/"
    val DEFAULT_BUILD_DIR = "build/intermediates/classes/debug/"
    var classpath: FileCollection? = null

    var DEFAULT_PATH = ".model"

    /**
     * default pakageName
     */
    var packageName: String? = "com.anbillon"
    @OutputDirectory
    var outputDir: File = project.file(DEFAULT_GEN_DIR)

    @TaskAction
    fun execute(inputs: IncrementalTaskInputs){
        LogUtil.log("task begin!")
        getInputs().files.forEach {
            it.whenNotNull {
                val fr:FileReader = FileReader(it)
                fr.forEachLine {
                    if(it.split(" ").first().toString().toUpperCase().contentEquals("GET")){
                        get(it.split(" ")[1])
                    }
                }
            }
        }
    }


    /**
     * query url
     */
    private fun get(url:String){
        LogUtil.log(url)
        val parsed = buildUri(url)
        parsed.decodedQueryDeduped.orEmpty().forEach { e->
            LogUtil.log(e.key)
            LogUtil.log(e.value)
        }
        url.httpGet().responseString { request, response, result ->
            result.fold({ d ->
                LogUtil.log(d)
                val parse:Parser = Parser()
                val dJson:JsonObject = parse.parse(d.byteInputStream()) as JsonObject
                generateBean(camalName(parsed.decodedPath?.get(0)),dJson,packageName+DEFAULT_PATH)

            }, { err ->
                LogUtil.log(err.message)
            })
        }
    }

    /**
     * name model by Camel-Case
     */
    private fun camalName(name:String?): String?{
        val first = name?.first()
        val last = name?.removeRange(0,1)
        return first?.toUpperCase().toString().plus(last)
    }

    private fun resolveFile(fileName: String): File {
        return File(fileName).let {
            if (it.isAbsolute) it
            else File(project.file(outputDir), fileName)
        }
    }



    private fun updateBean(file:File,json: JsonObject){
        if (file.exists()){

            var tool: JavaCompiler = ToolProvider.getSystemJavaCompiler()
            val result = tool.run(null,null,null,"-d","build/classes/test/",file.path)


            URLClassLoader.getSystemClassLoader().loadClass(file.path).fields.forEach {
                println(it.name)
            }
            val testClass = Class.forName(packageName+"."+file.name.split(".")[0])
            testClass.fields.forEach {
                var nameStr = it.name
                var typeStr = it.type
                json.forEach { m ->
                    if (m.value is Number) {
                    } else if (m.value is Boolean) {
                    } else if (m.value is String) {
                    }else{
                    }
                }
            }

        }
    }


    fun generateBean(name: String?,json:JsonObject,pacakge:String){

        val build:TypeSpec.Builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC)
        json.forEach { m ->
            if (m.value is Number) {
                LogUtil.log("long")
                build.addField(TypeName.LONG,m.key,Modifier.PUBLIC)
            } else if (m.value is Boolean) {
                LogUtil.log("boolean")
                build.addField(TypeName.BOOLEAN,m.key,Modifier.PUBLIC)
            } else if (m.value is String) {
                LogUtil.log("string")
                build.addField(String.toString().javaClass,m.key,Modifier.PUBLIC)
            }else{
                LogUtil.log("what?")
            }
        }
        val genType:TypeSpec = build.build()
        LogUtil.log("packge:"+pacakge)
        val javaFile = JavaFile.builder(pacakge, genType).build()
        if (outputDir.isDirectory){
            LogUtil.log("isDirectory")
            if (outputDir.canWrite()){
                var f:File = File(outputDir,name+".java")
                if (f.exists())
                    f.delete()
                LogUtil.log("canWrite")
                javaFile.writeTo(outputDir)
            }else throw Exception("can not write path:"+outputDir.absolutePath)
        }
    }
    private fun combinedClasspath(others: FileCollection?): Array<out URL>? {

        fun Iterable<File>.toUrls(): Sequence<URL> = asSequence().map { it.toURI().toURL() }

        return mutableListOf<URL>().apply {
            classpath?.let {
                it.toUrls().forEach { add(it) }
            }
            others?.let { it.toUrls().forEach { add(it) } }
        }.toTypedArray().apply { project.logger.debug("Classpath for generator: ${Arrays.toString(this)}") }
    }


//    fun generateFile(spec: GenerateSpec, classLoader: ClassLoader) {
//        if (spec.output != null) {
//            val outFile = resolveFile(spec.output!!)
//
//            if (spec.generator != null) {
//                val gname = spec.generator
//                val generatorClass = classLoader.loadClass(gname)
//                if (outFile.isDirectory) throw InvalidUserDataException("The output can not be a directory, it must be a file ($outFile)")
//                if (!outFile.exists()) {
//                    outFile.parentFile.apply { if (!exists()) mkdirs() || throw InvalidUserDataException("The target directory for the output file $outFile could not be created") }
//                    outFile.createNewFile()
//                }
//                if (!outFile.canWrite()) throw InvalidUserDataException("The output file ($outFile) is not writeable.")
//
//                if (project.logger.isInfoEnabled) {
//                    project.logger.info("Generating ${spec.name} as '${spec.output}' as '$outFile'")
//                } else {
//                    project.logger.lifecycle("Generating ${spec.name} as '${spec.output}'")
//                }
//
//                val baseError = """
//              Generators must have a unique public method "doGenerate(Writer|Appendable, [Object])"
//              where the second parameter is optional iff the input is null. If not a static
//              method, the class must have a noArg constructor.""".trimIndent()
////                generatorClass.execute({ outFile.writer() }, spec.input, baseError)
//
//            } else {
//                throw InvalidUserDataException("Missing output code for generateSpec ${spec.name}, no generator provided")
//            }
//        }
//    }

    private fun genrateMethod(name: String, map: Map<String,Any>){
         val build:TypeSpec.Builder = TypeSpec.classBuilder(name);


    }

//    private fun Class<out Any>.execute(firstParam: Any, input: Any?, baseErrorMsg: String) {
//        getGeneratorMethods(firstParam !is File, input).let { candidates ->
//            try {
//                var resolvedInput = input
//                var methodIterator = candidates
//                        .asSequence()
//                        .filter { if (it.parameterCountCompat == 1) true else isSecondParameterCompatible(input, it) }
//                        .iterator()
//
//                if (input is Callable<*> && !methodIterator.hasNext()) {
//                    resolvedInput = input.call()
//                    methodIterator = candidates.asSequence()
//                            .filter { isSecondParameterCompatible(resolvedInput, it) }.iterator()
//                }
//
//                if (!methodIterator.hasNext()) throw InvalidUserDataException(errorMsg("No candidate method found", candidates, baseErrorMsg, input))
//
//                val m = methodIterator.next()
//
//                if (methodIterator.hasNext()) {
//                    throw InvalidUserCodeException(ambiguousChoice(candidates, baseErrorMsg, input))
//                }
//                m.doInvoke(this, firstParam, resolvedInput)
//                return
//            } catch (e: Exception) {
//                throw InvalidUserDataException("Could not execute the generator code", e)
//            }
//        }
//    }
//    private fun Class<*>.getGeneratorMethods(firstParamWriter: Boolean, input: Any?): List<Method> {
//        return methods.asSequence()
//                .filter { it.name == "doGenerate" }
//                .filter { Modifier.isPublic(it.modifiers) }
//                .filter { if (input == null) it.parameterCountCompat in 1..2 else it.parameterCountCompat == 2 }
//                .filter {
//                    if (firstParamWriter) {
//                        Appendable::class.java.isAssignableFrom(it.parameterTypes[0]) && it.parameterTypes[0].isAssignableFrom(Writer::class.java)
//                    } else {
//                        File::class.java == it.parameterTypes[0]
//                    }
//                }.toList()
//    }

}

class GenerateSpec(val name: String) {
    var output: String? = null
    var generator: String? = null
    var packageName:String? = null
    var classpath: FileCollection? = null
    var input: Any? = null
}