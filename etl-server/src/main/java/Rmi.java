

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.linkage.ftpdrudgery.console.IFtpdrudgeryConsole;

public class Rmi {

	/**
	 * @param args
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		// TODO Auto-generated method stub
		IFtpdrudgeryConsole ic = (IFtpdrudgeryConsole)Naming.lookup("rmi://10.109.3.232:7777/ftpConsole");
		System.out.println(ic.getHump());
		ic.setHumP(1000l, 30);
	}

}
