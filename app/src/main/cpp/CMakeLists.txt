cmake_minimum_required(VERSION 3.22.1)

project("huabu")

# ---------------------------------------------------------------
# 16 KB page-size alignment (required for Android 15+ / NDK r28+)
# All shared libraries must be aligned to 16 KB segments.
# ---------------------------------------------------------------
if(ANDROID)
    # Force 16 KB ELF segment alignment
    add_link_options("-Wl,-z,max-page-size=16384")
    add_link_options("-Wl,-z,common-page-size=16384")
endif()

add_library(
    huabu_native
    SHARED
    huabu_native.cpp
)

find_library(log-lib log)

target_link_libraries(
    huabu_native
    ${log-lib}
)
