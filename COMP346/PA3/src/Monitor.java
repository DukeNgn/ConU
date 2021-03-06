import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 * @author Duc Nguyen -- student
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	enum STATES {eating, hungry, thinking};
	int numberOfChopsticks;
  private int NumberOfPhilosophers;

  final Lock lock; // A lock to provide synchronization state
	public static STATES[] state;
	public static Condition [] self;
  private static Condition talking;
  private static boolean someOneTalking;

	/**
	 * Constructor
	 */
	public Monitor(int NumberOfPhilosophers)
	{
    this.NumberOfPhilosophers = NumberOfPhilosophers;
    //Set number of chopsticks and states corresponding to the number of philosophers joining the party
		numberOfChopsticks = NumberOfPhilosophers;
		state = new STATES[NumberOfPhilosophers];
		self = new Condition [NumberOfPhilosophers];

    lock = new ReentrantLock();
    //Set initial states
    for(int i = 0; i < NumberOfPhilosophers; ++i) {
        state[i] = STATES.thinking;
        self[i] = lock.newCondition();
    }
    talking = lock.newCondition();
    someOneTalking = false;
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

  // A method used to test 2 sides of a philosopher (check if the current philosopher has enough chopsticks)
  public void test(int piTID) {
      lock.lock();
      try {
        if((state[(piTID - 1 + NumberOfPhilosophers) % NumberOfPhilosophers] != STATES.eating)
                && (state[piTID] == STATES.hungry)
                && (state[(piTID + 1) % NumberOfPhilosophers] != STATES.eating)) {

                //Eating
                state[piTID] = STATES.eating;
                self[piTID].signal();
                }
      } finally {
          lock.unlock();
      }
  }

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public void pickUp(final int piTID) {
      lock.lock();
      try {
          state[piTID] = STATES.hungry;
          test(piTID);
          if(state[piTID] != STATES.eating) {
              self[piTID].await();
          }
      } catch (InterruptedException e) {
          e.printStackTrace();
      } finally {
          lock.unlock();
      }

	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public void putDown(final int piTID)
	{
      lock.lock();
      try {
          state[piTID] = STATES.thinking;
          //Check and let right, left philosophers to pick up chopstick if they are waiting while hungry.
          test((piTID - 1 + NumberOfPhilosophers) % NumberOfPhilosophers);
          test((piTID + 1 + NumberOfPhilosophers) % NumberOfPhilosophers);
      } finally {
        lock.unlock();
      }
	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating: the condition is checked before calling requestTalk inside Philosopher class)
	 */
	public void requestTalk()
	{
    lock.lock();
    try {
        if(someOneTalking) {
            talking.await();
        }
        someOneTalking = true;
    } catch (InterruptedException e) {
        e.printStackTrace();
    } finally {
        lock.unlock();
    }
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public void endTalk()
	{
      lock.lock();
      try {
          someOneTalking = false;
          talking.signal();
      } finally {
          lock.unlock();
      }
	}
}

// EOF
