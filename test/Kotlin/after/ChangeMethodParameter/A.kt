package ChangeMethodParameter

import java.util.List

class A {
    fun foo(array: List<Integer?>?): Boolean {
        for (i in 0 until array.size()) {
            if (array.get(i) < 10) return true
        }
        return false
    }
}
