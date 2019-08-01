# AndroidSample

Android的一些示例，方便学习研究，其中Samples是主项目，Assist是辅助项目（跨应用操作需要）

## Activity生命周期与启动模式

Activity：A(standard)、B(singleTop)、C(singleTask)、D(singleInstance)

* 从A跳转到B：A.onPause -> B.onCreate -> B.onStart -> B.onResume -> A.onSaveInstanceState -> A.onStop
* 从B返回A：B.onPause -> A.onRestart -> A.onStart -> A.onResume -> B.onStop -> B.onDestroy
* LifecycleOwner与LifecycleObserver的生命周期方法执行顺序：进入的时候LifecycleOwner先于LifecycleObserver执行，而离开的时候相反，LifecycleObserver的onStop会先于LifecycleOwner的onSaveInstanceState执行
* 在B中打开B：onPause -> onNewIntent -> onResume
* 在C中打开A接着再打开C：C.onPause -> A.onCreate -> A.onStart -> A.onResume -> C.onSaveInstanceState -> C.onStop -> A.onPause -> C.onNewIntent -> C.onRestart -> C.onStart -> C.onResume -> A.onStop -> A.onDestroy
* 在A中打开D再打开B然后再按返回键：会回到A，但是D还活着，再打开D会执行：onNewIntent -> onRestart -> onStart -> onResume
* 一般情况下Activity在哪个任务栈中被打开，那么其就处于该任务栈中。但是在设置了singleInstance启动模式的Activity中打开会不一样，singleInstance表示Activity独占一个任务栈，在该Activity中打开的其他Activity会被放在前一个任务栈中。
* taskAffinity用来指定任务栈的名字，默认为包名，taskAffinity和singleTask或者allowTaskRearenting属性配对使用
* allowTaskRearenting属性是这样的：假定应用A打开应用B的Activity(allowTaskRearenting属性为ture)，那么该Activity会处于应用B的任务栈中，如果该Activity的allowTaskRearenting属性为false，则会处于应用A的任务栈中。从所处任务栈这一点来看，效果和设置Intent.FLAG_ACTIVITY_NEW_TASK打开该Activity是一样的，区别在按返回键上，allowTaskRearenting属性为ture的Activity按返回键时会自动创建应用的入口Activity，而仅设置Intent.FLAG_ACTIVITY_NEW_TASK的Activity会直接被结束。
