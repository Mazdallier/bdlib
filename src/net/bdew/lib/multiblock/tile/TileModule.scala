/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * https://raw.github.com/bdew/bdlib/master/MMPL-1.0.txt
 */

package net.bdew.lib.multiblock.tile

import net.bdew.lib.block.BlockRef
import net.bdew.lib.data.base.{TileDataSlots, UpdateKind}
import net.bdew.lib.multiblock.Tools
import net.bdew.lib.multiblock.data.DataSlotPos

import scala.reflect.ClassTag

trait TileModule extends TileDataSlots {
  val kind: String
  val connected = new DataSlotPos("connected", this).setUpdate(UpdateKind.WORLD, UpdateKind.SAVE, UpdateKind.RENDER)

  def getCoreAs[T <: TileController : ClassTag] = connected flatMap (_.getTile[T](getWorldObj))

  def getCore = getCoreAs[TileController]

  lazy val mypos = BlockRef(xCoord, yCoord, zCoord)

  def connect(target: TileController) {
    if (target.moduleConnected(this)) {
      connected.set(target.mypos)
      getWorldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      getWorldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType)
    }
  }

  def coreRemoved() {
    connected.unset()
    getWorldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    getWorldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType)
  }

  def onBreak() {
    getCore map (_.moduleRemoved(this))
  }

  def tryConnect() {
    if (getCore.isEmpty) {
      for {
        conn <- Tools.findConnections(getWorldObj, mypos, kind).headOption
        core <- conn.getTile[TileController](getWorldObj)
      } {
        connect(core)
      }
    }
  }
}
