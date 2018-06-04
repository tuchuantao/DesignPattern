# 一、六大设计原则
单一职责原则（SRP）：一个类只负责一个职责，不要存在多个导致类变更的原因
<br/>
开闭原则(OCP)： 对扩展开放，对修改封闭
<br/>
里氏替换原则(LSP)：子类必须能够替换他们的基类
<br/>
依赖倒置原则（DIP）：提高灵活性，高层不依赖低层，两个都应该依赖于抽象
<br/>
迪米特原则(LOD)：类间解耦，类之间联系尽可能少
<br/>
接口隔离原则（ISP）：客户端不应该依赖它不需要的接口，多用组合，少用继承
<br/><br/>
设计模式主要分三个类型:  (总计23种)
* 创建型：（5种) <br/>
&emsp;&emsp;工方法模式、抽象工厂模式、单例模式、建造者模式
* 结构型：（7种) <br/>
&emsp;&emsp;适配器模式、装饰模式、代理模式、外观模式、桥接模式、组合模式、享元模式
* 行为型：（11种）<br/>
&emsp;&emsp;策略模式、模板方法模式、观察者模式、迭代器模式、责任链模式、命令模式、备忘录模式、状态模式、访问者模式、中介者模式、解释器模式

<br/>
## UML图解：
<img src="./imgs/UML.png" alt="UML图解" align=center />
<br/>
***
# 二、创建型模式
创建型模式(Creational Pattern)对类的实例化过程进行了抽象，能够将软件模块中对象的创建和对象的使用分离。为了使软件的结构更加清晰，外界对于这些对象只需要知道它们共同的接口，而不清楚其具体的实现细节，使整个系统的设计更加符合单一职责原则。
创建型模式隐藏了类的实例的创建细节，通过隐藏对象如何被创建和组合在一起达到使整个系统独立的目的。
<br/>
## 工厂方法模式（Factory Method Pattern）
<b>简单工厂类图：</b><br/>
<img src="./imgs/SimpleFactory.jpg" alt="简单工厂类图" align=center /> <br/>
<b>工厂方法类图：</b><br/>
<img src="./imgs/FactoryMethod.png" alt="工厂方法类图" align=center /> <br/>
<br/>
工厂方法模式是简单工厂模式的进一步抽象和推广。由于使用了面向对象的多态性，工厂方法模式保持了简单工厂模式的优点，而且克服了它的缺点。在工厂方法模式中，核心的工厂类不再负责所有产品的创建，而是将具体创建工作交给子类去做。这个核心类仅仅负责给出具体工厂必须实现的接口，而不负责哪一个产品类被实例化这种细节，这使得工厂方法模式可以允许系统在不修改工厂角色的情况下引进新产品。
<b>使用反射实现工厂模式：</b>
<br/>
'''
public abstract class Factory {
  public abstract <T extends Product> T createProduct(Class<T> clz);
}

public class ConcreteFactory extends Factory {
  @Override
  public <T extends Product> T createProduct(Class<T> clz) {
    Product p = null;
    try {
      p = (Product) Class.from(clz.getName()).newInstance();
    } catch(Exception e) {
    }
    return (T) p;
  }
}
'''
