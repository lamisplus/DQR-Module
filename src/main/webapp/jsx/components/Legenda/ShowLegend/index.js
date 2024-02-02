import React, { useEffect, useState } from "react";
import {  Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";


const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 300,
  },
  button: {
    margin: theme.spacing(1),
  },
  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
    "& .card-title": {
      color: "#fff",
      fontWeight: "bold",
    },
    "& .form-control": {
      borderRadius: "0.25rem",
      height: "41px",
    },
    "& .card-header:first-child": {
      borderRadius: "calc(0.25rem - 1px) calc(0.25rem - 1px) 0 0",
    },
    "& .dropdown-toggle::after": {
      display: " block !important",
    },
    "& select": {
      "-webkit-appearance": "listbox !important",
    },
    "& p": {
      color: "red",
    },
    "& label": {
      fontSize: "14px",
      color: "#014d88",
      fontWeight: "bold",
    },
  },
  demo: {
    backgroundColor: theme.palette.background.default,
  },
  inline: {
    display: "inline",
  },
  error: {
    color: "#f85032",
    fontSize: "12.8px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
}));

  const LegendDQA = (props) => {
  const classes = useStyles();

  return (
    <>
      <Card className={classes.root}>
        <CardContent>
          <h3> Legend </h3>
          <div className="col-xl-12 col-lg-12">
            <Table bordered>
              <tbody>
                <tr>
                  <td> 95% and Above Score </td>
                  <td>
                  <div>
                                <span style={{ backgroundColor: "rgb(0,255,100)", color: "white",
                                  border: "2px solid", margin: "2px", padding: "8px", fontWeight: "bold"
                              }}
                                    > Very Good </span>
                                </div>
                    </td>
                </tr>
                <tr>
                  <td>
                    90% - 94% Score
                  </td>
                  <td>
                  <div>
                    <span style={{ backgroundColor: "rgb(255,255,0)", color: "white",
                     border: "2px solid", margin: "2px", padding: "8px", fontWeight: "bold",
                     boxSizing: "border-box"
                     }}
                     > Good </span>
                     </div>
                     </td>
                </tr>
                <tr>
                  <td>
                      89% and Below Score
                  </td>
                  <td>
                  <div>
                    <span style={{ backgroundColor: "rgb(255,0,100)", color: "white",
                     border: "2px solid", margin: "2px", padding: "8px", fontWeight: "bold",
                     boxSizing: "border-box"
                     }}
                     > Poor </span>
                     </div>
                     </td>
                  
                </tr>
                
                
              </tbody>
            </Table>           
          </div>
        </CardContent>
      </Card>
    </>
  );
};

export default LegendDQA;
