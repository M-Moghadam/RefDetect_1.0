package ChangeMethodParameter

import java.util.List

class B : A() {
    private fun foo(array: List<Integer?>?): Boolean {
        var i = 0
        while (i < array.size()) {
            if (array.get(i) < 10) return true
            i++
        }
        return false
    }
}
