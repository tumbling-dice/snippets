package inujini.linq.test;

import static inujini_.linq.Linq.*;
import inujini_.function.Function.Action1;
import inujini_.function.Function.Func1;
import inujini_.function.Function.Func2;
import inujini_.function.Function.Predicate;

import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) {

		List<Hoge> hogeList = new ArrayList<Hoge>();
		hogeList.add(new Hoge(0, "Hoge0"));
		hogeList.add(new Hoge(1, "Hoge1"));
		hogeList.add(new Hoge(2, "Hoge2"));

		//where
		System.out.println("where method test.");
		System.out.println("extract Hoge1 and Hoge2.");
		System.out.println("----------");

		linq(hogeList)
			.where(new Predicate<Hoge>(){
				public Boolean call(Hoge p1) {
					return p1.getId() > 0;
				}
			})
			.forEach(new Action1<Hoge>(){
				public void call(Hoge p1) {
					System.out.println(p1.getName());
				}
			});

		System.out.println("----------");

		/*//any
		System.out.println("any method test.");
		System.out.println("check on hogeList has Hoge0 and Hoge3.");
		System.out.println("----------");
		if(linq(hogeList).any(new R1<Boolean, Hoge>(){
								public Boolean call(Hoge p1) {
									return p1.getName().equals("Hoge0");
								}
							})){
			System.out.println("hogeList has Hoge0.");
		} else {
			System.out.println("hogeList does not have Hoge0.");
		}

		if(linq(hogeList).any(new R1<Boolean, Hoge>(){
								public Boolean call(Hoge p1) {
									return p1.getName().equals("Hoge3");
								}
							})){
			System.out.println("hogeList has Hoge3.");
		} else {
			System.out.println("hogeList does not have Hoge3.");
		}*/

		System.out.println("----------");

		//select
		System.out.println("select method test.");
		System.out.println("hogeList's Hoge.class convert to Fuga.class.");
		System.out.println("----------");

		linq(hogeList)
			.select(new Func1<Hoge, Fuga>(){
				private int age = 12;
				public Fuga call(Hoge p1) {
					return new Fuga(p1.getId(), age++);
				}
			})
			.forEach(new Action1<Fuga>() {
				public void call(Fuga p1) {
					System.out.println("Id:" + p1.getId().toString());
					System.out.println("Age:" + p1.getAge().toString());
				}
			});

		System.out.println("----------");

		//where + select
		System.out.println("where + select method test.");
		System.out.println("----------");
		linq(hogeList)
			.where(new Predicate<Hoge>(){
				public Boolean call(Hoge p1) {
					return p1.getId() > 0;
				}
			})
			.select(new Func1<Hoge, Fuga>(){
				private int age = 12;
				public Fuga call(Hoge p1) {
					return new Fuga(p1.getId(), age++);
				}
			})
			.forEach(new Action1<Fuga>() {
				public void call(Fuga p1) {
					System.out.println("Id:" + p1.getId().toString());
					System.out.println("Age:" + p1.getAge().toString());
				}
			});

		System.out.println("----------");

		//join
		List<Fuga> fugaList = new ArrayList<Fuga>();
		int age = 12;
		for(int i = 1; i < 3; i++) {
			Fuga fuga = new Fuga(i, age);
			fuga.setSex(age % 2 == 0 ? "male" : "female");
			fugaList.add(fuga);
			age++;
		}

		System.out.println("join method test.");
		System.out.println("hogeList's ids are 0, 1 and 2.");
		System.out.println("fugaList's ids are 1, 2 and 3.");
		System.out.println("hogeList joins fugaList on ids, and convert to Piyo");

		System.out.println("----------");
		linq(hogeList).join(fugaList,
					new Func1<Hoge, Integer>(){
						public Integer call(Hoge p1) {
							return p1.getId();
						}
					},
					new Func1<Fuga, Integer>(){
						public Integer call(Fuga p2) {
							return p2.getId();
						}
					},
					new Func2<Hoge, Fuga, Piyo>(){
						public Piyo call(Hoge p1, Fuga p2) {
							Piyo piyo = new Piyo();

							piyo.setId(p1.getId());
							piyo.setName(p1.getName());
							piyo.setAge(p2.getAge());
							piyo.setSex(p2.getSex());

							return piyo;
						}
					})
			.forEach(new Action1<Piyo>() {
				public void call(Piyo p1) {
					System.out.println("* * *");
					System.out.println("Id:" + p1.getId().toString());
					System.out.println("Name:" + p1.getName());
					System.out.println("Age:" + p1.getAge().toString());
					System.out.println("Sex:" + p1.getSex());
					System.out.println("* * *");
				}
			});
		System.out.println("----------");

		//takeWhile
		System.out.println("takeWhile method test.");
		System.out.println("extract Hoge0 and Hoge1.");
		System.out.println("----------");
		linq(hogeList)
			.takeWhile(new Predicate<Hoge>(){
				public Boolean call(Hoge p1) {
					return p1.getId() <= 1;
				}
			})
			.forEach(new Action1<Hoge>() {
				public void call(Hoge p1) {
					System.out.println(p1.getName());
				}
			});
		System.out.println("----------");

		//skipWhile
		System.out.println("skipWhile method test.");
		System.out.println("skip over Hoge0 and extract Hoge1 and Hoge2.");
		System.out.println("----------");
		linq(hogeList)
			.skipWhile(new Predicate<Hoge>(){
				public Boolean call(Hoge p1) {
					return p1.getId() <= 1;
				}
			})
			.forEach(new Action1<Hoge>() {
				public void call(Hoge p1) {
					System.out.println(p1.getName());
				}
			});
		System.out.println("----------");

	}

}