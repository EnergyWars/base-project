package com.wafflehq.lib.modules.permissions

object PermissionRegistryCheck {

    data class Result(
        val notInCatalog: Set<String> = emptySet(),
        val notInManifest: Set<String> = emptySet(),
        val definitionsWithoutUsage: Set<String> = emptySet(),
        val usagesWithoutDefinition: Set<String> = emptySet(),
        val duplicateIds: Set<String> = emptySet()
    ) {
        val isConsistent: Boolean
            get() = notInCatalog.isEmpty() &&
                notInManifest.isEmpty() &&
                definitionsWithoutUsage.isEmpty() &&
                usagesWithoutDefinition.isEmpty() &&
                duplicateIds.isEmpty()

        fun describe(): String = buildString {
            if (notInCatalog.isNotEmpty()) {
                appendLine("Im Manifest deklariert, aber ohne PermissionDefinition (oder implicitPermissions-Eintrag): ${notInCatalog.sorted()}")
            }
            if (notInManifest.isNotEmpty()) {
                appendLine("In einer PermissionDefinition genannt, aber nicht im Manifest deklariert: ${notInManifest.sorted()}")
            }
            if (definitionsWithoutUsage.isNotEmpty()) {
                appendLine("Ohne registrierte PermissionUsage - niemand erklaert, wofuer sie gebraucht wird: ${definitionsWithoutUsage.sorted()}")
            }
            if (usagesWithoutDefinition.isNotEmpty()) {
                appendLine("PermissionUsage zeigt auf eine unbekannte permissionId: ${usagesWithoutDefinition.sorted()}")
            }
            if (duplicateIds.isNotEmpty()) {
                appendLine("Mehrfach vergebene PermissionDefinition-id: ${duplicateIds.sorted()}")
            }
        }
    }

    fun verify(
        manifestPermissions: Set<String>,
        definitions: Set<PermissionDefinition>,
        usages: Set<PermissionUsage>,
        implicitPermissions: Set<String> = emptySet()
    ): Result {
        val covered = definitions.flatMap { it.manifestPermissions }.toSet()
        val definitionIds = definitions.map { it.id }
        val usedIds = usages.map { it.permissionId }.toSet()
        return Result(
            notInCatalog = manifestPermissions - covered - implicitPermissions,
            notInManifest = covered - manifestPermissions,
            definitionsWithoutUsage = definitionIds.toSet() - usedIds,
            usagesWithoutDefinition = usedIds - definitionIds.toSet(),
            duplicateIds = definitionIds.groupingBy { it }.eachCount()
                .filterValues { it > 1 }
                .keys
        )
    }
}
