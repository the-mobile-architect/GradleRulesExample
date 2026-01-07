package extensions

import org.gradle.api.provider.SetProperty

interface TechDebtExtension {
    // We use a Set so we don't have duplicates
    val flags: SetProperty<String>

    // Helper function to make the syntax cleaner: include("skipDesignCheck")
    fun include(vararg debts: String) {
        flags.addAll(debts.toList())
    }
}