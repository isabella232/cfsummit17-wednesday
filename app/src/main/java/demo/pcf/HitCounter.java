package demo.pcf;

import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;

@Component
public class HitCounter {

	private final IAtomicLong countAny;
	private final IAtomicLong countRoot;
	
	public HitCounter(HazelcastInstance hazelcastInstance) {
		this.countAny = hazelcastInstance.getAtomicLong(MyConstants.HIT_COUNTER_ANY_NAME);
		this.countRoot = hazelcastInstance.getAtomicLong(MyConstants.HIT_COUNTER_ROOT_NAME);
	}
	
	public void hitAnyPage() {
		this.countAny.incrementAndGet();
	}
	public void hitRootPage() {
		this.countRoot.incrementAndGet();
	}

	public long getCountAny() {
		return this.countAny.get();
	}
	public long getCountRoot() {
		return this.countRoot.get();
	}
}
