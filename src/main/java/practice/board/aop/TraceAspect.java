package practice.board.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TraceAspect {

    @Before(value = "@annotation(practice.board.aop.Trace)")
    public void doTraceBefore(JoinPoint joinPoint) {
        log.info("[doTraceBefore] {}, args={}", joinPoint.getSignature(), joinPoint.getArgs());
    }


}
