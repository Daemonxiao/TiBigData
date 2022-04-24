/*
 * Copyright 2021 TiDB Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tidb.bigdata.flink.connector.source.split;

import io.tidb.bigdata.tidb.SplitInternal;
import io.tidb.bigdata.tidb.TableHandleInternal;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import org.apache.flink.api.connector.source.SourceSplit;
import org.tikv.common.meta.TiTimestamp;

public class TiDBSourceSplit implements Serializable, SourceSplit {

  private final SplitInternal split;
  private final long offset;

  public TiDBSourceSplit(SplitInternal split, long offset) {
    this.split = split;
    this.offset = offset;
  }

  @Override
  public String splitId() {
    return split.toString();
  }

  public SplitInternal getSplit() {
    return split;
  }

  public long getOffset() {
    return offset;
  }

  @Override
  public int hashCode() {
    return split.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TiDBSourceSplit)) {
      return false;
    }
    return Objects.equals(split, ((TiDBSourceSplit) o).split);
  }

  public void serialize(DataOutputStream dos) throws IOException {
    dos.writeLong(offset);
    TableHandleInternal table = split.getTable();
    dos.writeUTF(table.getConnectorId());
    dos.writeUTF(table.getSchemaName());
    dos.writeUTF(table.getTableName());
    dos.writeUTF(split.getStartKey());
    dos.writeUTF(split.getEndKey());
    TiTimestamp timestamp = split.getTimestamp();
    dos.writeLong(timestamp.getPhysical());
    dos.writeLong(timestamp.getLogical());
  }

  public static TiDBSourceSplit deserialize(DataInputStream dis) throws IOException {
    long offset = dis.readLong();
    String connectorId = dis.readUTF();
    String schemaName = dis.readUTF();
    String tableName = dis.readUTF();
    String startKey = dis.readUTF();
    String endKey = dis.readUTF();
    long physical = dis.readLong();
    long logical = dis.readLong();
    return new TiDBSourceSplit(
        new SplitInternal(
            new TableHandleInternal(connectorId, schemaName, tableName),
            startKey,
            endKey,
            new TiTimestamp(physical, logical)),
        offset);
  }
}
