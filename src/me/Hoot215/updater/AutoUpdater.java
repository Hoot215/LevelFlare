/*
 * Fireworks display on levelling up.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.Hoot215.updater;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.plugin.java.JavaPlugin;

public class AutoUpdater implements Runnable
  {
    private JavaPlugin plugin;
    private String localVersion;
    private String remoteVersion;
    private String site;
    private AtomicBoolean upToDate = new AtomicBoolean(true);
    private AutoUpdaterPlayerListener playerListener;
    
    public AutoUpdater(JavaPlugin instance)
      {
        plugin = instance;
        localVersion = plugin.getDescription().getVersion();
        site = plugin.getDescription().getWebsite();
        playerListener = new AutoUpdaterPlayerListener(plugin, this);
      }
    
    public String getNewestVersion ()
      {
        return remoteVersion;
      }
    
    public String getSite ()
      {
        return site;
      }
    
    public boolean isUpToDate ()
      {
        return upToDate.get();
      }
    
    public void start ()
      {
        plugin.getServer().getPluginManager()
            .registerEvents(playerListener, plugin);
        new Thread(this).start();
      }
    
    public void run ()
      {
        while (true)
          {
            try
              {
                URL url =
                    new URL("http://dl.dropbox.com/u/56151340/BukkitPlugins/"
                        + plugin.getName() + "/latest");
                Scanner s = new Scanner(url.openStream());
                remoteVersion = s.nextLine();
                if (this.updateCheck())
                  {
                    upToDate.set(false);
                    plugin.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable()
                          {
                            public void run ()
                              {
                                plugin.getLogger().info(
                                    "A newer version of " + plugin.getName()
                                        + " is available! (v" + remoteVersion
                                        + ")");
                                if (site != null && !site.isEmpty())
                                  {
                                    plugin.getLogger().info(
                                        "Download it here: " + site);
                                  }
                              }
                          });
                  }
                s.close();
                Thread.sleep(3600000L);
              }
            catch (MalformedURLException e)
              {
                e.printStackTrace();
              }
            catch (InterruptedException e)
              {
                e.printStackTrace();
              }
            catch (IOException e)
              {
              }
            catch (NoSuchElementException e)
              {
              }
          }
      }
    
    private boolean updateCheck ()
      {
        String[] local = localVersion.split("\\.");
        String[] remote = remoteVersion.split("\\.");
        int longestNumber =
            local.length > remote.length ? local.length : remote.length;
        for (int i = 0; i < longestNumber; i++)
          {
            int l = 0;
            int r = 0;
            try
              {
                try
                  {
                    l = Integer.valueOf(local[i]);
                  }
                catch (ArrayIndexOutOfBoundsException e)
                  {
                  }
                try
                  {
                    r = Integer.valueOf(remote[i]);
                  }
                catch (ArrayIndexOutOfBoundsException e)
                  {
                  }
              }
            catch (NumberFormatException e)
              {
              }
            if (r > l)
              return true;
          }
        return false;
      }
  }