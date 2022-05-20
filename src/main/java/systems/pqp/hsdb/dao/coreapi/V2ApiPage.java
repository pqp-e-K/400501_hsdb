package systems.pqp.hsdb.dao.coreapi;

import java.util.List;

public class V2ApiPage {

    private Integer total;
    private Integer page;
    private Integer size;
    private V2ApiLink self;
    private V2ApiLink first;
    private V2ApiLink prev;
    private V2ApiLink next;
    private V2ApiLink last;

    private List<V2ApiLink> elements;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public V2ApiLink getSelf() {
        return self;
    }

    public void setSelf(V2ApiLink self) {
        this.self = self;
    }

    public V2ApiLink getFirst() {
        return first;
    }

    public void setFirst(V2ApiLink first) {
        this.first = first;
    }

    public V2ApiLink getPrev() {
        return prev;
    }

    public void setPrev(V2ApiLink prev) {
        this.prev = prev;
    }

    public V2ApiLink getNext() {
        return next;
    }

    public void setNext(V2ApiLink next) {
        this.next = next;
    }

    public V2ApiLink getLast() {
        return last;
    }

    public void setLast(V2ApiLink last) {
        this.last = last;
    }

    public List<V2ApiLink> getElements() {
        return elements;
    }

    public void setElements(List<V2ApiLink> elements) {
        this.elements = elements;
    }

    public boolean hasNext(){
        return null != getNext();
    }
}
