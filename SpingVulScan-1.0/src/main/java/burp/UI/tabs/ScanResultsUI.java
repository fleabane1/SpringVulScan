package burp.UI.tabs;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IMessageEditor;
import burp.IMessageEditorController;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class ScanResultsUI extends AbstractTableModel implements IMessageEditorController {
   private IBurpExtenderCallbacks callbacks;
   private IHttpRequestResponse currentHttp;
   private IMessageEditor messageRequest;
   private IMessageEditor messageResponse;
   private List<ScanResultsUI.TableData> tasks = new ArrayList();
   private JTabbedPane tabs;
   private JPanel ScannerUI;
   private JSplitPane mainSplitPane;
   private JSplitPane httpSplitPane;
   private JScrollPane tablePane;
   private JTabbedPane requestPane;
   private JTabbedPane responsePane;

   public ScanResultsUI(IBurpExtenderCallbacks callbacks, JTabbedPane tabs) {
      this.callbacks = callbacks;
      this.tabs = tabs;
      this.initUI();
      this.tabs.addTab("扫描流量", this.ScannerUI);
   }

   public void initUI() {
      this.ScannerUI = new JPanel(new BorderLayout());
      this.mainSplitPane = new JSplitPane(0);
      this.httpSplitPane = new JSplitPane(1);
      this.httpSplitPane.setDividerLocation(0.5D);
      ScanResultsUI.Table table = new ScanResultsUI.Table(this);
      this.tablePane = new JScrollPane(table);
      this.requestPane = new JTabbedPane();
      this.responsePane = new JTabbedPane();
      this.messageRequest = this.callbacks.createMessageEditor(this, false);
      this.messageResponse = this.callbacks.createMessageEditor(this, false);
      this.requestPane.addTab("Resquest", this.messageRequest.getComponent());
      this.responsePane.addTab("Response", this.messageResponse.getComponent());
      this.httpSplitPane.add(this.requestPane);
      this.httpSplitPane.add(this.responsePane);
      this.mainSplitPane.add(this.tablePane);
      this.mainSplitPane.add(this.httpSplitPane);
      this.ScannerUI.add(this.mainSplitPane);
   }

   public IHttpService getHttpService() {
      return this.currentHttp.getHttpService();
   }

   public byte[] getRequest() {
      return this.currentHttp.getRequest();
   }

   public byte[] getResponse() {
      return this.currentHttp.getResponse();
   }

   public int getRowCount() {
      return this.tasks.size();
   }

   public int getColumnCount() {
      return 8;
   }

   public Object getValueAt(int rowIndex, int columnIndex) {
      ScanResultsUI.TableData data = (ScanResultsUI.TableData)this.tasks.get(rowIndex);
      switch(columnIndex) {
      case 0:
         return data.id;
      case 1:
         return data.checkMethod;
      case 2:
         return data.requestMethod;
      case 3:
         return data.url;
      case 4:
         return data.status_code;
      case 5:
         return data.issue;
      case 6:
         return data.startTime;
      case 7:
         return data.endTime;
      default:
         return null;
      }
   }

   public String getColumnName(int column) {
      switch(column) {
      case 0:
         return "#";
      case 1:
         return "checkMethod";
      case 2:
         return "requestMethod";
      case 3:
         return "url";
      case 4:
         return "status_code";
      case 5:
         return "issue";
      case 6:
         return "startTime";
      case 7:
         return "endTime";
      default:
         return null;
      }
   }

   public Class<?> getColumnClass(int columnIndex) {
      return String.class;
   }

   public int add(String extensionMethod, String requestMethod, String url, String statusCode, String issue, IHttpRequestResponse requestResponse) {
      synchronized(this.tasks) {
         Date date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String startTime = sdf.format(date);
         int id = this.tasks.size();
         this.tasks.add(new ScanResultsUI.TableData(id, url, statusCode, requestMethod, extensionMethod, issue, startTime, "", requestResponse));
         this.fireTableRowsInserted(id, id);
         return id;
      }
   }

   public int save(int id, String extensionMethod, String requestMethod, String url, String statusCode, String issue, IHttpRequestResponse requestResponse) {
      ScanResultsUI.TableData dataEntry = (ScanResultsUI.TableData)this.tasks.get(id);
      String startTime = dataEntry.startTime;
      Date d = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String endTime = sdf.format(d);
      synchronized(this.tasks) {
         this.tasks.set(id, new ScanResultsUI.TableData(id, url, statusCode, requestMethod, extensionMethod, issue, startTime, endTime, requestResponse));
         this.fireTableRowsUpdated(id, id);
         return id;
      }
   }

   private static class TableData {
      final int id;
      final String url;
      final String status_code;
      final String requestMethod;
      final String checkMethod;
      final String issue;
      final String startTime;
      final String endTime;
      final IHttpRequestResponse iHttpRequestResponse;

      public TableData(int id, String url, String status_code, String method, String checkMethod, String issue, String startTime, String endTime, IHttpRequestResponse iHttpRequestResponse) {
         this.id = id;
         this.url = url;
         this.status_code = status_code;
         this.requestMethod = method;
         this.checkMethod = checkMethod;
         this.issue = issue;
         this.startTime = startTime;
         this.endTime = endTime;
         this.iHttpRequestResponse = iHttpRequestResponse;
      }
   }

   private class Table extends JTable {
      public Table(TableModel dm) {
         super(dm);
      }

      public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
         ScanResultsUI.TableData data = (ScanResultsUI.TableData)ScanResultsUI.this.tasks.get(this.convertRowIndexToModel(rowIndex));
         ScanResultsUI.this.messageRequest.setMessage(data.iHttpRequestResponse.getRequest(), true);
         ScanResultsUI.this.messageResponse.setMessage(data.iHttpRequestResponse.getResponse(), false);
         ScanResultsUI.this.currentHttp = data.iHttpRequestResponse;
         super.changeSelection(rowIndex, columnIndex, toggle, extend);
      }
   }
}
