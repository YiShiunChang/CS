
/**
 * “Add a Badger” button: when clicked, it settles a badger with random size from 10 to 100 into the sett, by calling your settleBadger() method in the Sett class. If an IllegalArgumentException is thrown from your implementation, the badger’s size will be replaced with another random number between  10 and 100. This process repeats until no exception is thrown.
“Switch to List Style”/”Switch to BST Style” button: click to switch between the two displaying styles.  When displaying in list style, it will display badgers in the order they appear in the list returned from your getAllBadger() method. With a correct implementation, you shall see badgers lining up, growing in size from left to right, up to down, as shown in Figure 2 at the beginning of this write-up.
“Clear Sett”: when clicked, it empties the sett. When the visualizer application finds the sett is too crowded so that you may not get a nice view, it will suggest you to clear the sett by showing “Sett is crowded, better to clear” on the button, as shown in Figure 1 at the beginning of this write up.
 * @author baliansnow
 *
 */
public class BadgersSettleDown {
  public static void main(String[] args) {
    // start the visualizer application
    SettVisualizer.startApplication();
  }
}