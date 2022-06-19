import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: release-cutter <json file> <target tag name>")
        exitProcess(1)
    }

    val jsonFile = args[0]
    val tagName = args[1]

    println("Tagging repos with $tagName from manifest $jsonFile")

    val packageModel = Json.parseToJsonElement(File(jsonFile).bufferedReader().readText())

    packageModel.jsonObject["packages"]?.jsonObject?.entries?.forEach { (name, pkg) ->
        val gitRepo = pkg.jsonObject["source"]?.jsonPrimitive?.content ?: error("Bad git repo for $pkg")
        val ref = pkg.jsonObject["ref"]?.jsonPrimitive?.content ?: error("Bad git ref for $pkg")

        //     git@github.com:regolith-linux/ilia.git
        // https://github.com/regolith-linux/ilia.git
        val repoPath = gitRepo.substring("https://github.com/".length)
        val internalRepo = "git@github.com:$repoPath"

        processRepo(name, internalRepo, tagName, ref)
    }
}

fun processRepo(name: String, internalRepo: String, tagName: String, ref: String) {
    println("git clone $internalRepo -b $ref")
    println("cd $name")
    println("git tag $tagName")
    println("git push origin $tagName")
    println("""printf "\n\n## Changes in \`$name\`:\n" >> /tmp/CHANGELOG.txt""")
    println("""echo "\`\`\`" >> /tmp/CHANGELOG.txt""")
    println("git log --since \"MAY 28 2022\" --until \"JUNE 06 2022\" --pretty=format:\"%h %s\" >> /tmp/CHANGELOG.txt")
    println("""echo "\`\`\`" >> /tmp/CHANGELOG.txt""")
    println("""echo >> /tmp/CHANGELOG.txt""")
    println("cd ..")
}
