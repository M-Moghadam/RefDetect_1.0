package ChangeMethodParameter;

import java.util.List;

public class A {

	public boolean foo(List<Integer> array){
		
		for (int i = 0; i < array.size(); i++) {
			
			if (array.get(i) < 10) return true;
		}
		
		return false;
	}
}
