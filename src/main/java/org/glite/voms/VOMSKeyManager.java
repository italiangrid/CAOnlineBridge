/*********************************************************************
 *
 * Authors: Vincenzo Ciaschini - Vincenzo.Ciaschini@cnaf.infn.it
 *
 * Copyright (c) Members of the EGEE Collaboration. 2004-2010.
 * See http://www.eu-egee.org/partners/ for details on the copyright holders.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Parts of this code may be based upon or even include verbatim pieces,
 * originally written by other people, in which case the original header
 * follows.
 *
 *********************************************************************/

package org.glite.voms;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.security.cert.X509Certificate;
import java.security.Security;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.PrivateKey;
import java.security.Principal;
import java.net.Socket;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

@SuppressWarnings("unused")
public class VOMSKeyManager implements X509KeyManager {
    private X509KeyManager manager = null;

    public static final int TYPE_PKCS12 = 1;
    public static final int TYPE_PEM    = 2;
    
    private String alias = "";

    private static final Logger logger = Logger.getLogger(VOMSKeyManager.class);

    static {
        if ( Security.getProvider( "BC" ) == null ) {
            Security.addProvider( new BouncyCastleProvider() );
        }
    }

    public VOMSKeyManager(String certfile, String keyfile, String password) {
        this(certfile, keyfile, password, TYPE_PEM);
    }

    public VOMSKeyManager(String certfile, String keyfile, String password, int type) {
        FileInputStream stream = null;

        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");

            char[] passwd = password.toCharArray();
            KeyStore keyStore = null;

            if (type == TYPE_PEM) {
                keyStore = KeyStore.getInstance("JKS");
                keyStore = load(certfile, keyfile, passwd);
            }
            else if (type == TYPE_PKCS12) {
            	
                keyStore = KeyStore.getInstance("PKCS12");
                stream = new FileInputStream(certfile);
                keyStore.load(stream, passwd);
                
                Enumeration<String> aliases = keyStore.aliases();
              
	            Enumeration<String> aliases2 = aliases;
	              
//	            while (aliases2.hasMoreElements()) {
//	  				String string = (String) aliases2.nextElement();
//	  				logger.info("ALIAS: "+ string);
//	  			}
	              
	              // Take the first alias and hope it is the right one...
	            this.alias = (String)aliases.nextElement();
	            
	            logger.info("ALIAS: " + this.alias);
            }

            if (keyStore != null) {
                keyManagerFactory.init(keyStore, passwd);
                manager = (X509KeyManager)keyManagerFactory.getKeyManagers()[0];
            }
            else {
                throw new Exception("Cannot initialize VOMSKeyManager: ");
            }
        }
        catch (Exception e) {
            logger.error("Cannot initialize VOMSKeyManager: ", e);
        }
        finally {
            try {
                if (stream != null)
                    stream.close();
            }
            catch(IOException e) {
                /* do nothing */
            }
        }
    }

    @SuppressWarnings("unchecked")
	private KeyStore createKeyStore(String cert, String key, char[] passwd) throws CertificateException, IOException {
        FileCertReader reader = new FileCertReader();

        X509Certificate[] certs = (X509Certificate[])reader.readCerts(cert).toArray(new X509Certificate[] {});

        PrivateKey pkey = null;
        KeyStore store = null;

        try {
            if (key != null)
                pkey = reader.readPrivateKey(key);
            else
                throw new Exception("Cannot load the private key.");
        }
        catch(IOException e) {
            logger.error("Cannot load the private key.", e);
        } catch (Exception e) {
			// TODO Auto-generated catch block
        	logger.error("Cannot load the private key.", e);
		}

        try {
            store = KeyStore.getInstance("JKS");
            store.setKeyEntry("alias", pkey, passwd, certs);
        } catch (KeyStoreException e) {
            logger.error("Cannot load the key pair.", e);
        }
        return store;
    }

    private KeyStore load(String certfile, String keyfile, char [] pwd) throws CertificateException, IOException {
        KeyStore store = null;

        if (!certfile.equals(keyfile)) {
            store = createKeyStore(certfile, keyfile, pwd);
        }
        else {
            store = createKeyStore(certfile, certfile, pwd);
        }
        return store;
    }

    public String chooseClientAlias(String[] keytype, Principal[] issuers, Socket socket) {
        return manager.chooseClientAlias(keytype, issuers, socket);
    }

    public String chooseServerAlias(String keytype, Principal[] issuers, Socket socket) {
        return manager.chooseServerAlias(keytype, issuers, socket);
    }

    public X509Certificate[] getCertificateChain(String alias) {
        return manager.getCertificateChain(this.alias);
    }

    public String[] getClientAliases(String keytype, Principal[] issuers) {
        return manager.getClientAliases(keytype, issuers);
    }

    public String[] getServerAliases(String keytype, Principal[] issuers) {
        return manager.getServerAliases(keytype, issuers);
    }

    public PrivateKey getPrivateKey(String alias) {
        return manager.getPrivateKey(this.alias);
    }
}