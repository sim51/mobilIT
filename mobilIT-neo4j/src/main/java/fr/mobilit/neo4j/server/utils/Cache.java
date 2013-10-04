/**
 * This file is part of MobilIT.
 *
 * MobilIT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobilIT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobilIT. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @See https://github.com/sim51/mobilIT
 */
package fr.mobilit.neo4j.server.utils;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.neo4j.server.logging.Logger;

public class Cache {

    private CacheManager         cacheManager;
    private net.sf.ehcache.Cache cache;
    private static Cache         uniqueInstance;
    private static final String  CACHENAME = "play";
    private static Logger LOGGER    = Logger.getLogger(Cache.class.getName());

    private Cache() {
        this.cacheManager = CacheManager.create();
        this.cacheManager.addCache(CACHENAME);
        this.cache = cacheManager.getCache(CACHENAME);
    }

    public static Cache getInstance() {
        return uniqueInstance;
    }

    public static Cache newInstance() {
        uniqueInstance = new Cache();
        return uniqueInstance;
    }

    public void add(String key, Object value, int expiration) {
        if (cache.get(key) != null) {
            return;
        }
        Element element = new Element(key, value);
        element.setTimeToLive(expiration);
        cache.put(element);
    }

    public void clear() {
        cache.removeAll();
    }

    public void delete(String key) {
        cache.remove(key);
    }

    public Object get(String key) {
        Element e = cache.get(key);
        return (e == null) ? null : e.getValue();
    }

    public void stop() {
        cacheManager.shutdown();
    }

}
