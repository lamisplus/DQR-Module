import React, { useEffect, useState } from "react";
import axios from "axios";
import MaterialTable from 'material-table';
import { token, url as baseUrl } from "../../../../api";
import {  Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import {  Button, } from "semantic-ui-react";

import { forwardRef } from 'react';
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";
import AddBox from '@material-ui/icons/AddBox';
import ArrowUpward from '@material-ui/icons/ArrowUpward';
import Check from '@material-ui/icons/Check';
import ChevronLeft from '@material-ui/icons/ChevronLeft';
import ChevronRight from '@material-ui/icons/ChevronRight';
import Clear from '@material-ui/icons/Clear';
import DeleteOutline from '@material-ui/icons/DeleteOutline';
import Edit from '@material-ui/icons/Edit';
import FilterList from '@material-ui/icons/FilterList';
import FirstPage from '@material-ui/icons/FirstPage';
import LastPage from '@material-ui/icons/LastPage';
import Remove from '@material-ui/icons/Remove';
import SaveAlt from '@material-ui/icons/SaveAlt';
import Search from '@material-ui/icons/Search';
import ViewColumn from '@material-ui/icons/ViewColumn';

const tableIcons = {
  Add: forwardRef((props, ref) => <AddBox {...props} ref={ref} />),
  Check: forwardRef((props, ref) => <Check {...props} ref={ref} />),
  Clear: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Delete: forwardRef((props, ref) => <DeleteOutline {...props} ref={ref} />),
  DetailPanel: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  Edit: forwardRef((props, ref) => <Edit {...props} ref={ref} />),
  Export: forwardRef((props, ref) => <SaveAlt {...props} ref={ref} />),
  Filter: forwardRef((props, ref) => <FilterList {...props} ref={ref} />),
  FirstPage: forwardRef((props, ref) => <FirstPage {...props} ref={ref} />),
  LastPage: forwardRef((props, ref) => <LastPage {...props} ref={ref} />),
  NextPage: forwardRef((props, ref) => <ChevronRight {...props} ref={ref} />),
  PreviousPage: forwardRef((props, ref) => <ChevronLeft {...props} ref={ref} />),
  ResetSearch: forwardRef((props, ref) => <Clear {...props} ref={ref} />),
  Search: forwardRef((props, ref) => <Search {...props} ref={ref} />),
  SortArrow: forwardRef((props, ref) => <ArrowUpward {...props} ref={ref} />),
  ThirdStateCheck: forwardRef((props, ref) => <Remove {...props} ref={ref} />),
  ViewColumn: forwardRef((props, ref) => <ViewColumn {...props} ref={ref} />)
  };

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

  const ValidityReportDQA = (props) => {
  const classes = useStyles();
  const [validity, setValidity] = useState({});
  const [showPatientDetail, setPatientDetail] = useState(false);
  const [getHeaderInfo, setGetHeaderInfo] = useState("");
  const [validityPatientsView, setValidityPatientsView] = useState([])

    useEffect(() => {
      loadValidity();
    }, []);

    
  const loadValidity = () => {
    axios
      .get(`${baseUrl}dqr/validity-summary`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setValidity(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const viewDetail =(headerTitle,patientDemoObj)=>{
  setPatientDetail(true)
  setGetHeaderInfo(headerTitle)
  const validityDemo =patientDemoObj
  axios
        .get(`${baseUrl}dqr/patient-validity?indicator=${validityDemo}`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        .then((response) => {
          setValidityPatientsView(response.data);
          //console.log(response.data[0])
        })
        .catch((error) => {
          console.log(error);
        });
  }
  const BackToList=()=> {
    setPatientDetail(false)
  }

  return (
    <>
      <Card className={classes.root}>
        <CardContent>
          <h3>Data Validation Report</h3>
          <div className="col-xl-12 col-lg-12">
          {!showPatientDetail &&(<>
            <Table bordered>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Complete Variables</th>
                  <th>Numerator</th>
                  <th>Denominator</th>
                  <th> Variance </th>
                  <th>Performance</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">1</th>
                  <td>
                      Proportion of all active patients with Date of birth after 1920
                  </td>
                  <td>{validity[0]?.normalDobNumerator}</td>
                  <td>{validity[0]?.normalDobDenominator}</td>
                  <td>{validity[0]?.normalDobVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.normalDobPerformance >= 95 ? 'green' : validity[0]?.normalDobPerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.normalDobPerformance >= 95 ? 'white' : validity[0]?.normalDobPerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.normalDobPerformance} %</td>
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                        onClick={() => viewDetail("Proportion of all active patients with Date of Birth (DOB)", "validity0" )}
                        >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">2</th>
                  <td>
                      Proportion of all active patients with Age at ART initiation (from ages of 0-90 years)
                  </td>
                  <td>{validity[0]?.ageInitiatedNumerator}</td>
                   <td>{validity[0]?.ageInitiatedDenominator}</td>
                   <td>{validity[0]?.ageInitiatedVariance} </td>
                   <td style={{ backgroundColor: validity[0]?.ageInitiatedPerformance >= 95 ? 'green' : validity[0]?.ageInitiatedPerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.ageInitiatedPerformance >= 95 ? 'white' : validity[0]?.ageInitiatedPerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.ageInitiatedPerformance} %</td>
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                        onClick={() => viewDetail("Proportion of all active patients with Age at ART initiation (from ages of 0-90 years)", "validity1" )}
                        >View</Button>
                  </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">3</th>
                  <td>
                      Proportion of patient with ART start date (from 1985 to current calendar year)
                  </td>
                  <td>{validity[0]?.startDateNumerator}</td>
                  <td>{validity[0]?.startDateDenominator}</td>
                  <td>{validity[0]?.startDateVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.startDatePerformance >= 95 ? 'green' : validity[0]?.startDatePerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.startDatePerformance >= 95 ? 'white' : validity[0]?.startDatePerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.startDatePerformance} %</td>
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                        onClick={() => viewDetail("Proportion of patient with ART start date (from 1985 to current calendar year)", "validity2" )}
                        >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">4</th>
                  <td>Proportion of eligible patient with First confirmed HIV test date (from 1985 to current calendar year)</td>
                  <td>{validity[0]?.hivDateNumerator}</td>
                  <td>{validity[0]?.hivDateDenominator}</td>
                  <td>{validity[0]?.hivDateVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.hivDatePerformance >= 95 ? 'green' : validity[0]?.hivDatePerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.hivDatePerformance >= 95 ? 'white' : validity[0]?.hivDatePerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.hivDatePerformance} %</td>
                  <td>
                    <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                        onClick={() => viewDetail("Proportion of eligible patient with First confirmed HIV test date (from 1985 to current calendar year)", "validity3" )}
                        >View</Button>
                  </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">5</th>
                  <td>
                      Proportion of patient with Date of viral load result (from 1985 to current calendar year)
                  </td>
                  <td>{validity[0]?.vlDateNumerator}</td>
                  <td>{validity[0]?.vlDateDenominator}</td>
                  <td>{validity[0]?.vlDateVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.vlDatePerformance >= 95 ? 'green' : validity[0]?.vlDatePerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.vlDatePerformance >= 95 ? 'white' : validity[0]?.vlDatePerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.vlDatePerformance} %</td>
                  <td>
                    <div>
                    <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                        onClick={() => viewDetail("Proportion of patient with Date of viral load result (from 1985 to current calendar year)", "validity6" )}
                        >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">6</th>
                  <td>
                      Proportion of all patients with valid Biometric fingerprint captured
                  </td>
                  <td>{validity[0]?.bioNumerator}</td>
                  <td>{validity[0]?.bioDenominator}</td>
                  <td>{validity[0]?.bioVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.bioPerformance >= 95 ? 'green' : validity[0]?.bioPerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.bioPerformance >= 95 ? 'white' : validity[0]?.bioPerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.bioPerformance} %</td>
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                      onClick={() => viewDetail("Proportion of all patients with valid Biometric fingerprint captured", "validity4" )}
                        >View</Button>
                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">7</th>
                  <td>
                      Proportion of patients with Days of ARV refill (between 14 - 180 days)
                  </td>
                  <td>{validity[0]?.regimenNumerator}</td>
                  <td>{validity[0]?.regimenDenominator}</td>
                  <td>{validity[0]?.regimenVariance} </td>
                  <td style={{ backgroundColor: validity[0]?.regimenPerformance >= 95 ? 'green' : validity[0]?.regimenPerformance >= 90 ? 'yellow' : 'red', color: validity[0]?.regimenPerformance >= 95 ? 'white' : validity[0]?.regimenPerformance >= 90 ? 'black' : 'white',
                    textAlign: 'center', fontWeight: 'bold' }}>{validity[0]?.regimenPerformance} %</td>
                  <td>
                  <div>
                  <Button style={{ backgroundColor: "rgb(153,46,98)" }} primary 
                      onClick={() => viewDetail("Proportion of patients with Days of ARV refill (between 14 - 180 days)", "validity4" )}
                        >View</Button>
                    </div>
                  </td>
                </tr>
                
              </tbody>
            </Table>
            </>)}
            {showPatientDetail &&(<>
                      <Button
                        variant="contained"
                        style={{backgroundColor:"#014d88", }}
                        className=" float-right mr-1"
                        //startIcon={<FaUserPlus />}
                        onClick={BackToList}
                        >
                        <span style={{ textTransform: "capitalize", color:"#fff" }}> {"<<"} Back </span>
                        </Button>
                        <br/>
                        <br/> 
                        <MaterialTable
                            icons={tableIcons}
                            title={getHeaderInfo}
                            columns={[

                              {
                                title: "Hospital Number",
                                field: "hospitalNumber",
                              },
                              { title: "Sex ", field: "sex", filtering: false },
                              { title: "Date Of Birth", field: "dob", filtering: false },
                              { title: "Status", field: "status", filtering: false },

                            ]}
                            data={ validityPatientsView.map((row) => ({
                              //Id: manager.id,
                              hospitalNumber: row.hospitalNumber,
                              sex: row.sex,
                              dob: row.dateOfBirth,
                              status:row.status

                            }))}

                            options={{
                              headerStyle: {
                                backgroundColor: "#014d88",
                                color: "#fff",
                              },
                              searchFieldStyle: {
                                width : '200%',
                                margingLeft: '250px',
                              },
                              filtering: false,
                              exportButton: true,
                              searchFieldAlignment: 'left',
                              pageSizeOptions:[10,20,100],
                              pageSize:10,
                              debounceInterval: 400
                            }}
                        />
            </>)}
          </div>
        </CardContent>
      </Card>
    </>
  );
};

export default ValidityReportDQA;
