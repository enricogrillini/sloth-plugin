package it.eg.sloth.mavenplugin.writer.refreshdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractDb implements DbIFace {

    String owner;

}
