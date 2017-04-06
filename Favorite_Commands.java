import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.JList;
import javax.swing.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import javax.swing.tree.*;

public class Favorite_Commands extends PlugInFrame implements MouseListener{
	private static final String CUSTOMIZE_FAVORITE_COMMANDS = "Customize Favorite Commands";
	JCommandList list = null;
	JTree tree;
	DefaultListModel<String> listModel =  null;
	Font font = null;	
	int fontSize = 12;
	public Favorite_Commands() {
		super("Favorite Commands");		
		//addTable();
		fontSize = Prefs.getInt(Prefs.MENU_SIZE, 12);
		//list.setRowHeight(fontSize + 10);
		font = new Font("SansSerif", Font.PLAIN, fontSize);	
		addList();
		pack();
		setSize(300, 600);
		GUI.center(this);
		setVisible(true);
	}	
	private DefaultListModel<String> loadListModel() {
		DefaultListModel<String> listModel = new DefaultListModel<String>();		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/FavoriteCommands.txt")));
			String line = br.readLine();
			while (line != null) {				
				String[] arr = line.split("\t");
				String commandName = arr[0];
				listModel.addElement(commandName);
				line = br.readLine();
			}
			br.close();
		}	
		catch (Exception ex) {
			IJ.log(ex.getMessage());
		}
		return listModel;
	}	
	
	private void saveListModel() {		
		try {
			File file = new File(getClass().getResource("/FavoriteCommands.txt").toURI());
			OutputStream output = new FileOutputStream(file);
			OutputStreamWriter out = new OutputStreamWriter(output, "UTF-8");
			BufferedWriter bw = new BufferedWriter(out);
			for (int i=0; i<listModel.getSize(); i++) {
				String str = listModel.getElementAt(i);
				bw.write(str);
				bw.newLine();
			}
			bw.close();
		}	
		catch (Exception ex) {
			IJ.log(ex.getMessage());
		}
	}
	private void addList() {
		listModel = loadListModel();		
		list = new JCommandList();
			
		list.setFont(font);
		list.setModel(listModel);

		JPanel pContainer = new JPanel();
		pContainer.setLayout(new BorderLayout());
		pContainer.add(list, BorderLayout.CENTER);
		JButton b = new JButton(CUSTOMIZE_FAVORITE_COMMANDS);
		b.setFont(font);
		b.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				customize();
			} 			
		});
		pContainer.add(b, BorderLayout.SOUTH);		
		JScrollPane scrollPane = new JScrollPane(pContainer);
		add("Center", scrollPane);	
		list.addMouseListener(this);
	}		
	public void mouseClicked(MouseEvent e) {		
		//int row = table.getSelectedRow();
		String cmd = list.getSelectedValue();
		runCommand(cmd);
	}
	
	private void recurseSubMenu(Menu menu, DefaultMutableTreeNode node) {
		int items = menu.getItemCount();
		if(items==0) return;
		for (int i=0; i<items; i++) {
			MenuItem mItem = menu.getItem(i);
			String label = mItem.getActionCommand();
			if (mItem instanceof Menu) {
				DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(label);
				recurseSubMenu((Menu)mItem,subNode);
				node.add(subNode);
			} else if (mItem instanceof MenuItem) {
				if (!(label.equals("-"))) {
					DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(label);
					node.add(leaf);					
				}
			}
		}
	}
	private synchronized DefaultMutableTreeNode doRootFromMenus() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("ImageJ Menus");
		MenuBar menuBar = Menus.getMenuBar();
		for (int i=0; i<menuBar.getMenuCount(); i++) {
			Menu menu = menuBar.getMenu(i);
			DefaultMutableTreeNode menuNode = new DefaultMutableTreeNode(menu.getLabel());
			recurseSubMenu(menu, menuNode);
			node.add(menuNode);
		}
		return node;
	}
	JTree buildTree() {
		DefaultMutableTreeNode root = doRootFromMenus();
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		JTree tree=new JTree(treeModel);
		tree.setFont(font);
		tree.setRowHeight(fontSize + 10);
		return tree;		
	}
	
	public void showCustomizeDialog() {		
		final JDialog frame = new JDialog(this, CUSTOMIZE_FAVORITE_COMMANDS, true);			
		ImageJ imageJ = IJ.getInstance();
		if (imageJ!=null && !IJ.isMacOSX()) {
			Image img = imageJ.getIconImage();
			if (img!=null)
				try {frame.setIconImage(img);} catch (Exception e) {}
		}
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());	
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());		
		final JList cList = new JList();
		int fontSize = Prefs.getInt(Prefs.MENU_SIZE, 12);
		Font font = new Font("SansSerif", Font.PLAIN, fontSize);		
		cList.setFont(font);
		cList.setModel(listModel);		
		JScrollPane scrollPane = new JScrollPane(cList);
		leftPanel.add(scrollPane, BorderLayout.CENTER);		
		JPanel buttons = new JPanel(new GridLayout(0, 1));
		buttons.setLayout(new GridLayout(0, 1));
		JLabel l = new JLabel();
		buttons.add(l);
		JButton button = null;		
        button = new JButton("Remove");
		button.setFont(font);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				int[] selectedIndices = cList.getSelectedIndices();
				for (int i = selectedIndices.length-1; i >=0; i--) {
				  listModel.removeElementAt(selectedIndices[i]);
				}
				saveListModel();
			} 
		});
		buttons.add(button);
		l = new JLabel();
		buttons.add(l);
		
		button = new JButton("<<< Add");
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
			// add
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (node == null) {
				return;
			}			
			if (node.isLeaf()) {
				Object nodeInfo = node.getUserObject();
				String cmd = nodeInfo.toString();
				listModel.addElement(cmd);
				saveListModel();
			}
		  } 
		});

		button.setFont(font);
		buttons.add(button);
	
		JPanel right = new JPanel(new BorderLayout());
		right.add(buttons, BorderLayout.NORTH);
		leftPanel.add(right, BorderLayout.EAST);
		
		contentPane.add(leftPanel, BorderLayout.WEST);
		
		tree = buildTree();
		JScrollPane ptView=new JScrollPane(tree);		
		contentPane.add(ptView, BorderLayout.CENTER);
		
		frame.pack();
		frame.setLocation(100,100);
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.toFront();
	}
	private void customize() {	
		showCustomizeDialog();		
	}
	private void runCommand(String command) {
		IJ.showStatus("Running command "+command);
		IJ.doCommand(command);
	}	
		
    public void mousePressed(MouseEvent e) {	}
    public void mouseReleased(MouseEvent e) {	}
    public void mouseEntered(MouseEvent e) {	}
    public void mouseExited(MouseEvent e) {		}
	
	class JCommandList extends JList<String> implements MouseListener, MouseMotionListener {
		int lastIndex = 0;
		public JCommandList() {
			super();
			init();
		}
		
		private void init() {			
			setFocusable(false);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		@Override
		public void mouseExited(MouseEvent mouseEvent) {
			this.clearSelection();
			lastIndex = -2;			
		}
		public void mousePressed(MouseEvent e) {		}
		public void mouseReleased(MouseEvent e) {		}
		public void mouseEntered(MouseEvent e) {		}
		public void mouseClicked(MouseEvent e) {	}
		public void  mouseDragged(MouseEvent e) {		}
		@Override
		public int locationToIndex(Point location) {
			int index = super.locationToIndex(location);
			if (index != -1 && !getCellBounds(index, index).contains(location)) {
				return -1;
			}
			else {
				return index;
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			final int i = locationToIndex(e.getPoint());			
			if (i != lastIndex) {
				lastIndex = i;
				clearSelection();
				if(i > -1){
					final Rectangle bounds = getCellBounds(i, i+1);
					if(bounds.contains(e.getPoint())){
						setSelectedIndex(i);
					}
				}
			}				
		}
	}
}
