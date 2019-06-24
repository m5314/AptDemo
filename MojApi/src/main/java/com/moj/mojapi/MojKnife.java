package com.moj.mojapi;

/**
 * @author : moj
 * @date : 2019/6/24
 * @description : 面向开发者的接入类
 */
public class MojKnife {

    public static void bind(Object obj){
        try{
            //目前只支持传入activity或View类型等有findViewById方法的对象
            String s = obj.getClass().getName() +"_ViewBinding";
            ViewBinding binding = (ViewBinding) obj.getClass().getClassLoader().loadClass(s).newInstance();
            binding.bindView(obj);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }
}
