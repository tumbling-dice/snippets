package inujini.linq.test;

public class Fuga {
	private Integer id;
	private Integer age;
	private String sex;

	public Fuga(Integer id, Integer age) {
		this.id = id;
		this.age = age;
	}

	public Integer getId() { return this.id; }
	public void setId(Integer id) { this.id = id; }
	public Integer getAge() { return this.age; }
	public void setAge(Integer age) { this.age = age; }
	public String getSex() { return this.sex; }
	public void setSex(String sex) { this.sex = sex; }
}
