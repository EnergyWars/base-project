val wafflehqLibDir: File =
    if (extra.has("wafflehqLibDir")) File(extra["wafflehqLibDir"] as String) else File(settingsDir, "lib")

check(wafflehqLibDir.isDirectory) { "WaffleHQ lib directory not found: $wafflehqLibDir" }

wafflehqLibDir
    .listFiles { candidate -> candidate.isDirectory && File(candidate, "build.gradle.kts").exists() }
    .orEmpty()
    .sortedBy { it.name }
    .forEach { module ->
        include(":lib:${module.name}")
        project(":lib:${module.name}").projectDir = module
    }
