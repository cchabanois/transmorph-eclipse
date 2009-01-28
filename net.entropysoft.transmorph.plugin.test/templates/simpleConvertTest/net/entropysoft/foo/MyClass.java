package net.entropysoft.foo;

import java.util.List;

public class MyClass {

	private List<Integer> listOfInts;
	private MyInnerClass myInnerClass;
	
	
	public List<Integer> getListOfInts() {
		return listOfInts;
	}

	public void setListOfInts(List<Integer> listOfInts) {
		this.listOfInts = listOfInts;
	}
	
	public void setMyInnerClass(MyInnerClass myInnerClass) {
		this.myInnerClass = myInnerClass;
	}

	public static class MyInnerClass {
		private String myString;

		public String getMyString() {
			return myString;
		}

		public void setMyString(String myString) {
			this.myString = myString;
		}
		
	}
	
	
}
