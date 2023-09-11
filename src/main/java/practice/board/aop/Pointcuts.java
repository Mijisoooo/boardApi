package practice.board.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    @Pointcut("execution(* *..*Repository.*(..))")
    public void allRepository() {}

    @Pointcut("execution(* *..*Service.*(..))")
    public void allService() {}

    @Pointcut("execution(* *..*Controller.*(..))")
    public void allController() {}

    @Pointcut("allRepository() && allService()")
    public void repositoryAndService() {}

}
