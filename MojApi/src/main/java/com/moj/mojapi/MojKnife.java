package com.moj.mojapi;

/**
 * @author : moj
 * @date : 2019/6/24
 * @description : Access classes for developers
 */
public class MojKnife {

    public static void bind(Object obj){
        try{
            //Currently only objects passing in findViewById methods, such as activity or View types, are supported
            String s = obj.getClass().getName() +"_ViewBinding";
            ViewBinding binding = (ViewBinding) obj.getClass().getClassLoader().loadClass(s).newInstance();
            binding.bindView(obj);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }
}
