package ChangeMethodParameter;

import java.util.List;

public class B extends A {

	private boolean foo(List<Integer> array){


		int i = 0;
		while (i < array.size()) {

			if (array.get(i) < 10) return true;

			i++;
		}

		return false;
	}
}
