import React, { useEffect, useState } from "react";
import axios from "axios";
import { token, url as baseUrl } from "../../../../api";
import { Form, Table } from "reactstrap";
import { makeStyles } from "@material-ui/core/styles";
import { Card, CardContent } from "@material-ui/core";
import "semantic-ui-css/semantic.min.css";
import { Dropdown, Button as Buuton2, Menu, Icon } from "semantic-ui-react";
import CloudUpload from "@material-ui/icons/CloudUpload";
import CloudDownloadIcon from "@material-ui/icons/CloudDownload";
import ErrorIcon from "@mui/icons-material/Error";
import { FiUploadCloud } from "react-icons/fi";
import "react-toastify/dist/ReactToastify.css";
import "react-widgets/dist/css/react-widgets.css";

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

const DemographicsDQA = (props) => {
  const classes = useStyles();
  const [demographics, setDemographic] = useState({});
  const [facilities, setFacilities] = useState([]);
  const [showPatientDetail, setPatientDetail] = useState(false);
    useEffect(() => {
      Facilities();
      loadDemography();
    }, []);
  const Facilities = () => {
    axios
      .get(`${baseUrl}account`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setFacilities(response.data.currentOrganisationUnitId);
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const loadDemography = () => {
    axios
      .get(`${baseUrl}dqr/patient-demo-summary?facilityId=${facilities}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((response) => {
        setDemographic(response.data);
        console.log(response.data)
      })
      .catch((error) => {
        console.log(error);
      });
  };

  const viewDetail =()=>{
  setPatientDetail(true)
  return alert("this is here")
  }


  return (
    <>
      <Card className={classes.root}>
        <CardContent>
          <h3>Demographics Variables</h3>
          <div className="col-xl-12 col-lg-12">
          {!showPatientDetail &&(<>
            <Table bordered>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Complete Variables</th>
                  <th>Numerator</th>
                  <th>Denominator</th>
                  <th>Performance</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">1</th>
                  <td>
                    Proportion of all active patients with Date of Birth (DOB)
                  </td>
                  <td>{demographics[0]?.dobNumerator}</td>
                  <td>{demographics[0]?.dobDenominator}</td>
                  <td>{demographics[0]?.dobPerformance} %</td>
                  <td>
                  <div>

                        <p onClick={() => viewDetail()}> View</p>

                    </div>
                  </td>
                </tr>
                <tr>
                  <th scope="row">2</th>
                  <td>Proportion of all active patients with Current Age</td>
                  <td>{demographics[0]?.ageNumerator}</td>
                   <td>{demographics[0]?.ageDenominator}</td>
                   <td>{demographics[0]?.agePerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">3</th>
                  <td>
                    Proportion of all active patients with Patient Identifier
                  </td>
                  <td>{demographics[0]?.pidNumerator}</td>
                  <td>{demographics[0]?.pidDenominator}</td>
                  <td>{demographics[0]?.pidPerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">4</th>
                  <td>Proportion of all active patients with Sex</td>
                  <td>{demographics[0]?.sexNumerator}</td>
                  <td>{demographics[0]?.sexDenominator}</td>
                  <td>{demographics[0]?.sexPerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">5</th>
                  <td>
                    Proportion of all active patients with a documented
                    educational Status
                  </td>
                  <td>{demographics[0]?.eduNumerator}</td>
                  <td>{demographics[0]?.eduDenominator}</td>
                  <td>{demographics[0]?.eduPerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">6</th>
                  <td>
                    Proportion of all active patients with a documented marital
                  </td>
                  <td>{demographics[0]?.maritalNumerator}</td>
                  <td>{demographics[0]?.maritalDenominator}</td>
                  <td>{demographics[0]?.maritalPerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">7</th>
                  <td>
                    Proportion of all active patients with documented occupational status
                  </td>
                  <td>{demographics[0]?.employNumerator}</td>
                  <td>{demographics[0]?.employDenominator}</td>
                  <td>{demographics[0]?.employPerformance} %</td>
                  <td></td>
                </tr>
                <tr>
                  <th scope="row">8</th>
                  <td>
                    Proportion of all active patients with registered
                    address/LGA of residence
                  </td>
                  <td>{demographics[0]?.addressNumerator}</td>
                  <td>{demographics[0]?.addressDenominator}</td>
                  <td>{demographics[0]?.addressPerformance} %</td>
                  <td>
                    <div>
                                  <Menu.Menu position="right">
                                    <Menu.Item>
                                      <Buuton2
                                        style={{ backgroundColor: "rgb(153,46,98)" }}
                                        primary
                                      >
                                        <Dropdown item text="Action">
                                          <Dropdown.Menu style={{ marginTop: "10px" }}>
                                            <Dropdown.Item
                                              //onClick={() => downloadFile(row.fileName)}
                                            >
                                              <CloudDownloadIcon color="primary" /> Download File
                                            </Dropdown.Item>


                                          </Dropdown.Menu>
                                        </Dropdown>
                                      </Buuton2>
                                    </Menu.Item>
                                  </Menu.Menu>
                                </div>
                  </td>
                </tr>
              </tbody>
            </Table>
            </>)}
            {showPatientDetail &&(<>
                        <h3>Patient Detail</h3>
                        <Table bordered>
                          <thead>
                            <tr>
                              <th>#</th>
                              <th>Complete Variables</th>
                              <th>Numerator</th>

                            </tr>
                          </thead>
                          <tbody>
                            <tr>
                              <th scope="row">1</th>
                              <td>
                                Proportion of all active patients with Date of Birth (DOB)
                              </td>
                              <td>{demographics[0]?.dobNumerator}</td>
                              <td>{demographics[0]?.dobDenominator}</td>
                              <td>{demographics[0]?.dobPerformance} %</td>
                              <td>
                              <div>

                                    <p onClick={() => viewDetail()}> View</p>

                                </div>
                              </td>
                            </tr>
                            <tr>
                              <th scope="row">2</th>
                              <td>Proportion of all active patients with Current Age</td>
                              <td>{demographics[0]?.ageNumerator}</td>
                               <td>{demographics[0]?.ageDenominator}</td>
                               <td>{demographics[0]?.agePerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">3</th>
                              <td>
                                Proportion of all active patients with Patient Identifier
                              </td>
                              <td>{demographics[0]?.pidNumerator}</td>
                              <td>{demographics[0]?.pidDenominator}</td>
                              <td>{demographics[0]?.pidPerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">4</th>
                              <td>Proportion of all active patients with Sex</td>
                              <td>{demographics[0]?.sexNumerator}</td>
                              <td>{demographics[0]?.sexDenominator}</td>
                              <td>{demographics[0]?.sexPerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">5</th>
                              <td>
                                Proportion of all active patients with a documented
                                educational Status
                              </td>
                              <td>{demographics[0]?.eduNumerator}</td>
                              <td>{demographics[0]?.eduDenominator}</td>
                              <td>{demographics[0]?.eduPerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">6</th>
                              <td>
                                Proportion of all active patients with a documented marital
                              </td>
                              <td>{demographics[0]?.maritalNumerator}</td>
                              <td>{demographics[0]?.maritalDenominator}</td>
                              <td>{demographics[0]?.maritalPerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">7</th>
                              <td>
                                Proportion of all active patients with documented occupational status
                              </td>
                              <td>{demographics[0]?.employNumerator}</td>
                              <td>{demographics[0]?.employDenominator}</td>
                              <td>{demographics[0]?.employPerformance} %</td>
                              <td></td>
                            </tr>
                            <tr>
                              <th scope="row">8</th>
                              <td>
                                Proportion of all active patients with registered
                                address/LGA of residence
                              </td>
                              <td>{demographics[0]?.addressNumerator}</td>
                              <td>{demographics[0]?.addressDenominator}</td>
                              <td>{demographics[0]?.addressPerformance} %</td>
                              <td>
                                <div>
                                              <Menu.Menu position="right">
                                                <Menu.Item>
                                                  <Buuton2
                                                    style={{ backgroundColor: "rgb(153,46,98)" }}
                                                    primary
                                                  >
                                                    <Dropdown item text="Action">
                                                      <Dropdown.Menu style={{ marginTop: "10px" }}>
                                                        <Dropdown.Item
                                                          //onClick={() => downloadFile(row.fileName)}
                                                        >
                                                          <CloudDownloadIcon color="primary" /> Download File
                                                        </Dropdown.Item>


                                                      </Dropdown.Menu>
                                                    </Dropdown>
                                                  </Buuton2>
                                                </Menu.Item>
                                              </Menu.Menu>
                                            </div>
                              </td>
                            </tr>
                          </tbody>
                        </Table>
                        </>)}
          </div>
        </CardContent>
      </Card>
    </>
  );
};

export default DemographicsDQA;
