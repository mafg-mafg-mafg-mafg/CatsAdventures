// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // CORREGIR: Usar alias para Hilt en lugar de id directo
    alias(libs.plugins.hilt.android) apply false
}

// ELIMINAR el buildscript - NO es necesario cuando usas version catalogs