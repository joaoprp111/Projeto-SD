//usando o conceito de fase (epoch)!!!
package src.Cliente;

import java.util.concurrent.locks.*;

public class Barreira {
	int leitura = 1; //leitura=0 -> é preciso ler; leitura=1 -> é preciso receber
  	
  	private Lock l=new ReentrantLock();
  	private Condition cond= l.newCondition();

	
	Barreira () {}

	void await(int n) throws InterruptedException {
		l.lock();
		try{
			if(n==1){
				this.leitura=0;
				cond.signalAll();
			}
			if(n==0){
				while(this.leitura==1)
					cond.await();
				leitura=1;
			}
		}finally{
			l.unlock();
		}
	}
}


