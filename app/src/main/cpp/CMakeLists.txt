# CMakeLists.txt for MitreGram Native Library
# Optimized for ARMv7 (armeabi-v7a) - Maximum Performance Build

cmake_minimum_required(VERSION 3.18.0)

# Project name and version
project("MitreGramNative" VERSION 1.0.0 LANGUAGES C CXX)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

# Android NDK specific configuration
if(ANDROID)
    # Force ARMv7 architecture
    set(ANDROID_ABI "armeabi-v7a")
    set(ANDROID_STL "c++_shared")
    set(ANDROID_TOOLCHAIN "clang")
    
    # ARM-specific optimization flags
    set(CMAKE_C_FLAGS_INIT "-O3 -ffast-math -funroll-loops -fomit-frame-pointer -march=armv7-a -mfpu=neon")
    set(CMAKE_CXX_FLAGS_INIT "-O3 -ffast-math -funroll-loops -fomit-frame-pointer -march=armv7-a -mfpu=neon")
    
    add_definitions("-DANDROID")
    add_definitions("-D_NATIVE_NDK")
endif()

# Build shared library - libQASM.so (MitreGramNative)
add_library(
    QASM
    SHARED
    mitre_gram.cpp
)

# Find and link Android log library
find_library(
    log-lib
    log
)

# Link log library
target_link_libraries(
    QASM
    ${log-lib}
    android
)

# Set compilation flags - visibility and optimizations
target_compile_options(
    QASM
    PRIVATE
    -Wall
    -Wextra
    -Wpedantic
    -fPIC
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
)

# Position-independent code for shared library
set_target_properties(
    QASM
    PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    OUTPUT_NAME "QASM"
    PREFIX ""
)

# Strip symbols for smaller binary (but no obfuscation)
add_custom_command(
    TARGET QASM POST_BUILD
    COMMAND ${ANDROID_STRIP} --strip-all $<TARGET_FILE:QASM>
    COMMENT "Stripping symbols from libQASM.so"
)

# Install configuration (for reference)
install(TARGETS QASM)
