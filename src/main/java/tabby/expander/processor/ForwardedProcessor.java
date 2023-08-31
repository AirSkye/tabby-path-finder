package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;
import tabby.data.Pollution;
import tabby.data.Cache;
import tabby.data.TabbyState;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class ForwardedProcessor implements Processor<TabbyState> {

    private TabbyState nextState;
    private Pollution pollution;

    @Override
    public void init(Node node, TabbyState preState, Relationship lastRelationship) {
        this.nextState = TabbyState.of();

        if(lastRelationship == null){
            this.pollution = preState.get("node_"+node.getId());
        }else{
            long id = lastRelationship.getId();
            this.pollution = preState.get(String.valueOf(id));
        }
    }

    @Override
    public Relationship process(Relationship next) {
        Relationship ret = null;
        String nextId = String.valueOf(next.getId());
        if(Types.isAlias(next)){
            Node start = next.getStartNode();
            Node end = next.getEndNode();
            Pollution nextPollution = Pollution.pure(start, end, pollution);
            if(nextPollution != null){
                nextState.put(nextId, nextPollution);
                ret = next;
            }
        }else{
            Pollution callSite = Cache.rel.get(next);

            Pollution nextPollution = Pollution.getNextPollution(pollution, callSite);
            if(nextPollution != null){
                nextState.put(nextId, nextPollution);
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        return true;
    }

    @Override
    public TabbyState getNextState() {
        return nextState;
    }

    @Override
    public void setCalculator(Calculator calculator) {
    }

    @Override
    public boolean isLastRelationshipTypeAlias() {
        return false;
    }

    @Override
    public Processor<TabbyState> copy() {
        return new ForwardedProcessor();
    }

    @Override
    public Processor<TabbyState> reverse() {
        return new BackwardedProcessor();
    }
}