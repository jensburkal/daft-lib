package com.daftsolutions.lib.pool;

import com.canto.cumulus.AllCategoriesItemCollection;
import com.canto.cumulus.Catalog;
import com.canto.cumulus.CategoryItem;
import com.canto.cumulus.CategoryItemCollection;
import com.canto.cumulus.CumulusException;
import com.canto.cumulus.GUID;
import com.canto.cumulus.ItemCollection;
import com.canto.cumulus.Layout;
import com.canto.cumulus.RecordItem;
import com.canto.cumulus.RecordItemCollection;
import com.canto.cumulus.Server;
import com.canto.cumulus.constants.CombineMode;
import com.canto.cumulus.constants.FindFlag;
import com.canto.cumulus.exceptions.QueryParserException;
import com.daftsolutions.lib.utils.CumulusUtilities;
import com.daftsolutions.lib.ws.dam.DamConnectionInfo;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author colin
 */
public class CumulusConnectionPool {

    private static Logger logger = Logger.getLogger(CumulusConnectionPool.class);
    private DamConnectionInfo connection = null;
    private int serverCount = 0;
    private int cloneCount = 0;
    private Server[] servers = null;
    private Catalog[] catalogs = null;
    private PoolMonitor poolMonitor = null;
    private Server masterServer = null;
    private Catalog masterCatalog = null;
    private RecordItemCollection masterRecordCollection = null;
    private CategoryItemCollection masterCategoryCollection = null;
    private Layout masterRecordLayout = null;
    private Layout masterCategoryLayout = null;
    private boolean enterpriseServer = false;
    private final ConcurrentLinkedQueue<ItemCollection> availableReadRecordCollections = new ConcurrentLinkedQueue<ItemCollection>();
    private final ConcurrentLinkedQueue<ItemCollection> availableReadCategoryCollections = new ConcurrentLinkedQueue<ItemCollection>();
    private final ConcurrentLinkedQueue<ItemCollection> availableWriteRecordCollections = new ConcurrentLinkedQueue<ItemCollection>();
    private final ConcurrentLinkedQueue<ItemCollection> availableWriteCategoryCollections = new ConcurrentLinkedQueue<ItemCollection>();

    /**
     *
     */
    public CumulusConnectionPool() {
    }

    /**
     *
     */
    public void refreshmasterRecordCollection() {
        masterRecordCollection.findAll();
    }

    /**
     *
     */
    public void refreshmasterCategoryCollection() {
        masterCategoryCollection.findAll();
    }

    /**
     *
     * @return
     */
    public Layout getRecordLayout() {
        return masterRecordLayout;
    }

    /**
     *
     * @return
     */
    public Layout getCategoryLayout() {
        return masterCategoryLayout;
    }

    /**
     *
     * @param connection
     * @param serverCount
     * @return
     */
    public boolean init(DamConnectionInfo connection) {
        boolean result = false;
        logger.info("Initialising CumulusConnectionPool with " + connection.licenseCount + " licenses and " + connection.cloneCount + " clones");
        this.connection = connection;
        this.serverCount = connection.licenseCount;
        this.cloneCount = connection.cloneCount;

        //TODO just playing for now
        Calendar cal = Calendar.getInstance();
        long nowMillis = cal.getTimeInMillis();
        cal.set(2010, 4, 1);
        long endMillis = cal.getTimeInMillis();
        if (nowMillis > endMillis) {
            System.out.println("Cannot start server - contact Daft.");
            return result;
        }

        try {
            // at least one server required - should be read/write, as Cumulus Workgroup will use license anyway
            masterServer = Server.openConnection(!connection.readOnly, connection.host, connection.username, connection.password);
            enterpriseServer = CumulusUtilities.isCumulusEnterpriseServer(masterServer);
            if (enterpriseServer) {
                logger.info("Cumulus Enterprise Server detected.");
                // master server should be read only to support unlimited reads
                masterServer = Server.openConnection(false, connection.host, connection.username, connection.password);
            } else {
                logger.info("Cumulus Workflow Server assumed.");

            }
            this.connection.id = masterServer.findCatalogID(connection.catalogName);
            masterCatalog = masterServer.openCatalog(this.connection.id);
            masterRecordCollection = masterCatalog.newRecordItemCollection(true);
            masterCategoryCollection = masterCatalog.newCategoryItemCollection();
            masterRecordCollection.findAll();
            masterCategoryCollection.findAll();
            masterRecordLayout = masterRecordCollection.getLayout();
            masterCategoryLayout = masterCategoryCollection.getLayout();
            if (connection.readOnly || enterpriseServer) {
                availableReadRecordCollections.offer(masterRecordCollection);
                availableReadCategoryCollections.offer(masterCategoryCollection);
            } else {
                availableWriteRecordCollections.offer(masterRecordCollection);
                availableWriteCategoryCollections.offer(masterCategoryCollection);
            }
            for (int j = 1; j < this.cloneCount; j++) {
                if (connection.readOnly || enterpriseServer) {
                    availableReadRecordCollections.offer(masterRecordCollection.clone());
                    availableReadCategoryCollections.offer(masterCategoryCollection.clone());
                } else {
                    availableWriteRecordCollections.offer(masterRecordCollection.clone());
                    availableWriteCategoryCollections.offer(masterCategoryCollection.clone());
                }
            }

            if (serverCount > 0) {
                // setup writable servers
                servers = new Server[serverCount];
                catalogs = new Catalog[serverCount];
                for (int i = 0; i < this.serverCount; i++) {
                    servers[i] = Server.openConnection(!connection.readOnly, connection.host, connection.username, connection.password);
                    catalogs[i] = servers[i].openCatalog(this.connection.id);
                    setupServerCollections(catalogs[i]);
                }
            }
            poolMonitor = new PoolMonitor();
            poolMonitor.start();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void setupServerCollections(Catalog catalog) {
        RecordItemCollection rc = catalog.newRecordItemCollection(true);
        CategoryItemCollection cc = catalog.newCategoryItemCollection();
        if (connection.readOnly) {
            availableReadRecordCollections.offer(rc);
            availableReadCategoryCollections.offer(cc);
        } else {
            availableWriteRecordCollections.offer(rc);
            availableWriteCategoryCollections.offer(cc);
        }
        for (int j = 1; j < this.cloneCount; j++) {
            if (connection.readOnly) {
                availableReadRecordCollections.offer(rc.clone());
                availableReadCategoryCollections.offer(cc.clone());
            } else {
                availableWriteRecordCollections.offer(rc.clone());
                availableWriteCategoryCollections.offer(cc.clone());
            }
        }

    }

    /**
     * Terminate the pool and close down the pool monitor
     */
    public void terminate() {
        try {
            servers = null;
            catalogs = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (poolMonitor != null) {
                poolMonitor.interrupt();
            }
        }
    }

    public boolean isEnterpriseServer() {
        return enterpriseServer;
    }

    /**
     *
     * @param collectionClass
     * @return
     */
    public ItemCollection borrowObjectToRead(Class collectionClass) {
        return borrowObjectToRead(collectionClass, false);
    }

    /**
     * Clone an object from the pool.
     * It is the responsibility of the caller to close the collection
     * This is required if collections are to be used in multi catalog collections, as they can be closed
     * outside of the control of the pool.
     *
     * @param collectionClass
     * @return
     */
    public ItemCollection cloneObjectToRead(Class collectionClass) {
        ItemCollection result = null;
        if (collectionClass == RecordItemCollection.class) {
            result = masterRecordCollection.clone();
        } else if (collectionClass == CategoryItemCollection.class) {
            result = masterCategoryCollection.clone();
        }
        return result;
    }

    /**
     * Borrow an object form the pool for reading
     * It is the responsibility of the borrower to return the object
     * @param collectionClass
     * @param refetch
     * @return
     */
    public ItemCollection borrowObjectToRead(Class collectionClass, boolean refetch) {
        ItemCollection result = null;
        // try the read queue first, and if none, then try the write queue
        synchronized (this) {
            if (collectionClass == RecordItemCollection.class) {
                if (connection.readOnly || enterpriseServer) {
                    result = availableReadRecordCollections.poll();
                } else {
                    result = availableWriteRecordCollections.poll();
                }
            } else if (collectionClass == CategoryItemCollection.class) {
                if (connection.readOnly || enterpriseServer) {
                    result = availableReadCategoryCollections.poll();
                } else {
                    result = availableWriteCategoryCollections.poll();
                }
            }
        }
        if (result != null && refetch) {
            result.findAll();
        }
        return result;
    }

    /**
     *
     * @param toWrite
     * @return
     */
    public AllCategoriesItemCollection getAllCategoriesItemCollection(boolean toWrite) {
        return (toWrite) ? catalogs[0].getAllCategoriesItemCollection() : masterCatalog.getAllCategoriesItemCollection();
    }

    /**
     *
     * @return
     */
    public AllCategoriesItemCollection getAllCategoriesItemCollection() {
        return getAllCategoriesItemCollection(false);
    }

    /**
     *
     * @return
     */
    public Catalog getMasterCatalog() {
        return masterCatalog;
    }

    /**
     *
     * @return
     */
    public Server getMasterServer() {
        return masterServer;
    }

    /**
     * Get a record item by id -if not found, update the master collection and try again
     * As this is one of the most common things to do, lets manage it in the pool, and provide it as a utility
     * @param id
     * @param toWrite
     * @return
     */
    public RecordItem getRecordItemById(int id, boolean toWrite) {
        RecordItem result = null;
        RecordItemCollection collection = null;
        try {
            if (!toWrite) {
                collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class);
            } else {
                collection = (RecordItemCollection) borrowObjectToWrite(RecordItemCollection.class);
            }
            try {
                result = collection.getRecordItemByID(id);
            } catch (Exception re) {
                // ignore so that we can retry
            }
            if (result == null) {
                masterRecordCollection.addItemByID(id);
                collection.addItemByID(id);
                result = collection.getRecordItemByID(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (collection != null) {
                if (!toWrite) {
                    returnReadObject(collection);
                } else {
                    returnWriteObject(collection);
                }
            }
        }
        return result;
    }

    /**
     *
     * @param query
     * @param locale
     * @return
     * @throws QueryParserException
     */
    public Integer findRecord(String query, String locale) throws QueryParserException {
        Integer result = -1;
        RecordItemCollection collection = null;
        try {
            collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class);
            collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_ARE_ERROR), CombineMode.FIND_NEW, getLocale(locale));
            if (collection.getItemCount() > 0) {
                result = collection.getItemIDs(0, 1).get(0);
            } else {
                // refetch and retry
                returnReadObject(collection);
                collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, true);
                collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_ARE_ERROR), CombineMode.FIND_NEW, getLocale(locale));
                if (collection.getItemCount() > 0) {
                    result = collection.getItemIDs(0, 1).get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnReadObject(collection);
        }
        return result;
    }

    /**
     *
     * @param query
     * @param offset
     * @param count
     * @param locale
     * @return
     * @throws QueryParserException
     * @throws CumulusException
     */
    public RecordResultSet findRecords(String query, Integer offset, Integer count, String locale) throws QueryParserException, CumulusException {
        RecordResultSet result = new RecordResultSet();
        RecordItemCollection collection = null;
        try {
            collection = (RecordItemCollection) borrowObjectToRead(RecordItemCollection.class, true);
            //TODO, pass in sorting field as parameter
            try {
                collection.setSorting(GUID.UID_REC_ASSET_CREATION_DATE);
            } catch (Exception se) {
                // just ignore if not indexed for sorting
            }
            collection.find(query, EnumSet.of(FindFlag.FIND_MISSING_FIELDS_ARE_ERROR), CombineMode.FIND_NEW, getLocale(locale));
            int collectionOffset = (offset <= 0) ? 0 : offset;
            int collectionCount = (count <= 0) ? collection.getItemCount() : count;
            if (offset < collection.getItemCount()) { // if offset out of range, return empty list
                if (collectionOffset + collectionCount > collection.getItemCount()) {
                    collectionCount = collection.getItemCount() - collectionOffset;
                }
                result.offset = collectionOffset;
                result.totalCount = collection.getItemCount();
                result.data = collection.getItemIDs(offset, collectionCount).toArray(new Integer[0]);
                result.count = result.data.length;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnReadObject(collection);
        }
        return result;
    }

    /**
     *
     * @param recordItem
     */
    public void releaseReadRecordItem(RecordItem recordItem) {
        if (recordItem != null) {
            returnReadObject(recordItem.getItemCollection());
        }
    }

    /**
     *
     * @param recordItem
     */
    public void releaseWriteRecordItem(RecordItem recordItem) {
        if (recordItem != null) {
            returnWriteObject(recordItem.getItemCollection());
        }
    }

    /**
     *
     * @param categoryItem
     * @param toWrite
     */
    public void releaseCategoryItem(CategoryItem categoryItem, boolean toWrite) {
        if (categoryItem != null) {
            returnObject(categoryItem.getItemCollection(), toWrite);
        }
    }

    /**
     *
     * @param categoryItem
     */
    public void releaseReadCategoryItem(CategoryItem categoryItem) {
        if (categoryItem != null) {
            returnReadObject(categoryItem.getItemCollection());
        }
    }

    /**
     *
     * @param categoryItem
     */
    public void releaseWriteCategoryItem(CategoryItem categoryItem) {
        if (categoryItem != null) {
            returnWriteObject(categoryItem.getItemCollection());
        }
    }

    /**
     *
     * @param recordItem
     * @param toWrite
     */
    public void releaseRecordItem(RecordItem recordItem, boolean toWrite) {
        if (recordItem != null) {
            returnObject(recordItem.getItemCollection(), toWrite);
        }
    }

    /**
     *
     * @param id
     * @param toWrite
     * @return
     */
    public CategoryItem getCategoryItemById(int id, boolean toWrite) {
        CategoryItem result = null;
        CategoryItemCollection collection = null;
        try {
            if (!toWrite) {
                collection = (CategoryItemCollection) borrowObjectToRead(CategoryItemCollection.class);
            } else {
                collection = (CategoryItemCollection) borrowObjectToWrite(CategoryItemCollection.class);
            }
            result = collection.getCategoryItemByID(id);
            if (result == null) {
                masterCategoryCollection.addItemByID(id);
                collection.addItemByID(id);
                result = collection.getCategoryItemByID(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param ids
     */
    public void deletetCategoryItems(Integer[] ids) {
        CategoryItemCollection collection = null;
        try {
            collection = (CategoryItemCollection) borrowObjectToWrite(CategoryItemCollection.class);
            for (Integer id : ids) {
                CategoryItem item = null;
                try {
                    collection.getCategoryItemByID(id);
                } catch (Exception e) {
                    // probably needs to be added to collection, so just ignore
                }
                if (item == null) {
                    masterCategoryCollection.addItemByID(id);
                    collection.addItemByID(id);
                    item = collection.getCategoryItemByID(id);
                }
                if (item != null) {
                    item.deleteItem();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param ids
     * @param deleteAssets
     */
    public void deleteRecordItems(Integer[] ids, boolean deleteAssets) {
        RecordItemCollection collection = null;
        try {
            collection = (RecordItemCollection) borrowObjectToWrite(RecordItemCollection.class);
            for (Integer id : ids) {
                logger.debug("attempt to delete record with id: " + id + " from catalog: '" + masterCatalog.getName() + "'");
                RecordItem item = null;
                try {
                    collection.getRecordItemByID(id);
                } catch (Exception e) {
                    // probably needs to be added to collection, so just ignore
                }
                if (item == null) {
                    masterRecordCollection.addItemByID(id);
                    collection.addItemByID(id);
                    item = collection.getRecordItemByID(id);
                }
                if (item != null) {
                    item.deleteItem(deleteAssets);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param collectionClass
     * @param toWrite
     * @return
     */
    public ItemCollection borrowObject(Class collectionClass, boolean toWrite) {
        if (!toWrite) {
            return borrowObjectToRead(collectionClass);
        } else {
            return borrowObjectToWrite(collectionClass);
        }
    }

    /**
     *
     * @param collection
     * @param toWrite
     */
    public void returnObject(ItemCollection collection, boolean toWrite) {
        if (!toWrite) {
            returnReadObject(collection);
        } else {
            returnWriteObject(collection);
        }
    }

    /**
     *
     * @param collectionClass
     * @return
     */
    public ItemCollection borrowObjectToWrite(Class collectionClass) {
        ItemCollection result = null;
        if (connection.readOnly) {
            logger.info("attempt to get writable object from read only catalog definition");
            return result;
        }
        synchronized (this) {
            if (collectionClass == RecordItemCollection.class) {
                synchronized (availableWriteRecordCollections) {
                    result = availableWriteRecordCollections.poll();
                }
            } else if (collectionClass == CategoryItemCollection.class) {
                synchronized (availableWriteCategoryCollections) {
                    result = availableWriteCategoryCollections.poll();
                }
            }
        }
        return result;
    }

    public void returnReadObject(ItemCollection collection) {
        if (collection == null) {
            return;
        }
        try {
            synchronized (this) {
                if (collection instanceof RecordItemCollection) {
                    if (connection.readOnly || enterpriseServer) {
                        availableReadRecordCollections.offer(collection);
                    } else {
                        availableWriteRecordCollections.offer(collection);
                    }
                } else if (collection instanceof CategoryItemCollection) {
                    if (connection.readOnly || enterpriseServer) {
                        availableReadCategoryCollections.offer(collection);
                    } else {
                        availableWriteCategoryCollections.offer(collection);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void returnWriteObject(ItemCollection collection) {
        if (collection == null) {
            return;
        }
        try {
            synchronized (this) {
                if (collection instanceof RecordItemCollection) {
                    availableWriteRecordCollections.offer(collection);
                } else if (collection instanceof CategoryItemCollection) {
                    availableWriteCategoryCollections.offer(collection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reconnectMasterServer() throws Exception {
        synchronized (this) {
            logger.info("Reconnecting master server");
            try {
                masterRecordCollection.close();
                masterCategoryCollection.close();
            } catch (Exception e) {
                // ignoe
            }

            masterServer = null;
            masterCatalog = null;
            masterServer = Server.openConnection(!connection.readOnly, connection.host, connection.username, connection.password);
            enterpriseServer = CumulusUtilities.isCumulusEnterpriseServer(masterServer);
            if (enterpriseServer) {
                // master server should be read only to support unlimited reads
                masterServer = Server.openConnection(false, connection.host, connection.username, connection.password);
            }
            masterCatalog = masterServer.openCatalog(connection.id);
            masterRecordCollection = masterCatalog.newRecordItemCollection(false);
            masterCategoryCollection = masterCatalog.newCategoryItemCollection();
            masterRecordCollection.findAll();
            masterCategoryCollection.findAll();
            masterRecordLayout = masterRecordCollection.getLayout();
            masterCategoryLayout = masterCategoryCollection.getLayout();
        }
    }

    private void closeServerCollections(ConcurrentLinkedQueue<ItemCollection> queue, Server server) {
        try {
            synchronized (queue) {
                ItemCollection ic = queue.poll();
                while (ic != null) {
                    if (ic.getCatalog().getServer().equals(server)) {
                        ic.close();
                    }
                    ic = queue.poll();
                }
            }
        } catch (Exception re) {
            // ignore
        }
    }

    private void reconnectServer(int serverIndex) throws Exception {
        synchronized (this) {
            logger.info("Reconnecting server index " + serverIndex);
            try {
                Server server = servers[serverIndex];
                closeServerCollections(availableReadRecordCollections, server);
                closeServerCollections(availableReadCategoryCollections, server);
                closeServerCollections(availableWriteRecordCollections, server);
                closeServerCollections(availableWriteCategoryCollections, server);
            } catch (Exception re) {
                // ignore
            }
            catalogs[serverIndex] = null;
            servers[serverIndex] = null;
            servers[serverIndex] = Server.openConnection(true, connection.host, connection.username, connection.password);
            catalogs[serverIndex] = servers[serverIndex].openCatalog(connection.id);
            setupServerCollections(catalogs[serverIndex]);
        }
    }

    private Locale getLocale(String locale) {
        return (locale != null && !"".equals(locale)) ? new Locale(locale) : Locale.getDefault();
    }

    private class PoolMonitor extends Thread {

        public final static int DEFAULT_FREQUENCY = 30000; // every 30 seconds
        public final static int LOG_CYCLE = 10; // log every
        private int checkFrequency = DEFAULT_FREQUENCY;

        public PoolMonitor() {
        }

        @Override
        public void run() {
            int c = 0;
            while (true) {
                try {
                    if (masterServer == null || !masterServer.isAlive()) {
                        reconnectMasterServer();
                    }
                    if (!connection.readOnly && servers != null) {
                        for (int i = 0; i < servers.length; i++) {
                            if (!servers[i].isAlive()) {
                                logger.info("PoolMonitor checking things - pool server disconnected - reconnecting ...");
                                reconnectServer(i);
                            }
                        }
                    }
                    if (masterServer != null && masterServer.isAlive()) {
                        if (masterRecordCollection != null) {
                            masterRecordCollection.findAll();
                        }
                        if (masterCategoryCollection != null) {
                            masterCategoryCollection.findAll();
                        }
                    }
                    // only print status on some runs, otherwise log file becomes huge with pointless information
                    if (c >= LOG_CYCLE) {
                        /*
                        logger.debug("PoolMonitor - open record collections for reading: " + readRecordItemCollectionCount + " - open category collections for reading: " + readCategoryItemCollectionCount);
                        if (!connection.readOnly) {
                        logger.debug("PoolMonitor - open record collections for writing: " + writeRecordItemCollectionCount + " - open category collections for writing: " + writeCategoryItemCollectionCount);
                        }
                         */
                        c = 0;
                    } else {
                        c++;
                    }

                    // all OK, so reset checkFrequency
                    checkFrequency = DEFAULT_FREQUENCY;
                } catch (Exception e) {
                    logger.info("PoolMonitor detected error in connection pool for catalog: '" + connection.catalogName + "' - check Tomcat logs for more details.");
                    e.printStackTrace();
                    // slow down reporting if continued failing
                    checkFrequency += 100;
                } finally {
                    try {
                        sleep(checkFrequency);
                    } catch (InterruptedException ie) {
                        logger.info("PoolMonitor interrupted and terminating.");
                        break;
                    }
                }
            }
        }
    }
}
