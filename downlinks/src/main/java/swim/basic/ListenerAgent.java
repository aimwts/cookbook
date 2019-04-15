package swim.basic;

import java.util.Map;
import swim.api.SwimLane;
import swim.api.agent.AbstractAgent;
import swim.api.downlink.MapDownlink;
import swim.api.lane.CommandLane;
import swim.api.lane.MapLane;
import swim.collections.HashTrieMap;
import swim.structure.Form;
import swim.structure.Record;
import swim.structure.Value;
import swim.recon.Recon;

public class ListenerAgent extends AbstractAgent {

  // Immutable java.util.Map containing all downlink subscriptions
  private HashTrieMap<String, MapDownlink<String, Integer>> shoppingCartSubscribers;

  // Shopping cart data for *all* UnitAgents, aggregated into a single ListenerAgent
  @SwimLane("shoppingCarts")
  public MapLane<String, Value> shoppingCarts = this.<String, Value>mapLane()
      .didUpdate((k, n, o) -> {
        logMessage("shoppingCarts updated " + k + ": " + Recon.toString(n));
      });

  // Opens a subscription to the `UnitAgent` indicated by `v`
  @SwimLane("triggerListen")
  public CommandLane<String> triggerListen = this.<String>commandLane()
      .onCommand(v -> {
        logMessage("will listen to " + v);
        addSubscription(v);
      });

  private void logMessage(Object msg) {
    System.out.println(nodeUri() + ": " + msg);
  }

  private void addSubscription(final String targetNode) {
    final MapDownlink<String, Integer> downlink = downlinkMap()
        .keyForm(Form.forString()).valueForm(Form.forInteger())
        .nodeUri(targetNode).laneUri("shoppingCart")
        .didUpdate((k, n, o) -> {
          // Update the correct entry in shoppingCarts on every downlink didUpdate()
          final Value shoppingCart = this.shoppingCarts.get(targetNode);
          final Record record = shoppingCart.isDefined() ? ((Record) shoppingCart).branch() : Record.create();
          record.put(k, n);
          this.shoppingCarts.put(targetNode, record);
        })
        .open();
    // Make this downlink accessible by adding it to `shoppingCartSubscribers`
    if (this.shoppingCartSubscribers == null) {
      this.shoppingCartSubscribers = HashTrieMap.empty();
    }
    this.shoppingCartSubscribers = this.shoppingCartSubscribers.updated(targetNode, downlink);
  }
}