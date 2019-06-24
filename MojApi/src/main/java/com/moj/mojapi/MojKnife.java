package com.moj.mojapi;

/**
 * @author : moj
 * @date : 2019/6/24
 * @description :
 */
public class MojKnife {

    public static void bind(Object obj){
        try{
            String s = obj.getClass().getName() +"_ViewBinding";
            ViewBinding binding = (ViewBinding) obj.getClass().getClassLoader().loadClass(s).newInstance();
            binding.bindView(obj);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }
}
