/**
 * A {@code Buffer} adds storage functionality namely the ability for producers
 * and consumers to store and retrieve resources without waiting for one another. 
 * The {@code put} and {@code get} are synchronized functions to prevent race condition.
 * In addition, when buffer if full or empty, original thread automatically waits 
 * for {@code this.notifyall} event to occur - where conditions would evaluate to false.
 * 
 * Three other features are provided as well:
 * {@link #peek()} which returns null if {@code isEmpty} else it returns the first object in the queue
 * {@link #isFull()} which returns a boolean: if all space in the buffer has been used up
 * {@link #isEmpty()} which returns a booelan: if the front index is -1
 * 
 * @param s  determines the size of the buffer to initialise
 */
public class Buffer {
    int SIZE, front, rear;
    Object items[] = null;
    int item_count = 0;

    /* Constructor to initialise class */
    public Buffer(int size) {
        this.SIZE = size;
        items = new Object[SIZE];
        front = -1;
        rear = -1;
    }

    /* Get first object in the queue or null if empty */
    public Object peek() {
        if (isEmpty()) {
            return null;
        }
        return items[front];
    }

    /* Check if the queue is full */
    public boolean isFull() {
        if (front == 0 && rear == SIZE - 1) {
            return true;
        }
        if (front == rear + 1) {
            return true;
        }
        return false;
    }

    /* Check if the queue is empty */
    public boolean isEmpty() {
        if (front == -1)
            return true;
        else
            return false;
    }

    /* Enqueue objects into the buffer */
    public synchronized void put(Object object) {
        while (isFull()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        if (front == -1) {
            front = 0;
        }
        rear = (rear + 1) % SIZE;
        items[rear] = object;

        System.out.println("Item count: " + (item_count + 1) + ", Produce 🟢 " + object);
        item_count++;
        this.notifyAll();
    }

    /* Dequeue objects into the buffer */
    public synchronized Object get() {
        Object element;
        while (isEmpty()) {
            System.out.println("Buffer is full currently");
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        element = items[front];
        if (front == rear) {
            front = -1;
            rear = -1;
        }
        else {
            front = (front + 1) % SIZE;
        }
        System.out.println("Item count: " + (item_count - 1) + ", Consume 🔴 " + element);
        item_count--;
        this.notifyAll();

        return element;
    }
}